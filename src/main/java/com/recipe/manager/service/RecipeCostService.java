package com.recipe.manager.service;

import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeCostService {

    private final RecipeRepository recipeRepository;
    private final RecipeCostRepository recipeCostRepository;
    private final IngredientPriceRepository ingredientPriceRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public RecipeCost getRecipeCost(Long recipeId, User currentUser) {
        validateCostViewPermission(currentUser);
        return recipeCostRepository.findByRecipeId(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("RecipeCost for recipe", recipeId));
    }

    @Transactional
    public RecipeCost calculateAndSave(Long recipeId, User currentUser) {
        validateCostViewPermission(currentUser);

        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        BigDecimal totalCost = calculateTotalIngredientCost(recipe);

        RecipeCost recipeCost = recipeCostRepository.findByRecipeId(recipeId)
                .orElse(RecipeCost.builder()
                        .recipe(recipe)
                        .build());

        recipeCost.setTotalIngredientCost(totalCost);
        recipeCost.setLastCalculatedAt(LocalDateTime.now());

        BigDecimal marginRate = recipeCost.getTargetGrossMarginRate();
        if (marginRate.compareTo(BigDecimal.ONE) < 0) {
            BigDecimal recommended = totalCost.divide(
                    BigDecimal.ONE.subtract(marginRate), 2, RoundingMode.HALF_UP);
            recipeCost.setRecommendedPrice(recommended);
        }

        RecipeCost saved = recipeCostRepository.save(recipeCost);
        log.info("Recipe cost calculated: recipeId={}, totalCost={}, recommendedPrice={}, by={}",
                recipeId, totalCost, saved.getRecommendedPrice(), currentUser.getEmail());
        return saved;
    }

    @Transactional
    public RecipeCost updateRecipeCost(Long recipeId, UpdateRecipeCostRequest request, User currentUser) {
        validateCostUpdatePermission(currentUser);

        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        RecipeCost recipeCost = recipeCostRepository.findByRecipeId(recipeId)
                .orElse(RecipeCost.builder()
                        .recipe(recipe)
                        .build());

        if (request.getTargetGrossMarginRate() != null) {
            recipeCost.setTargetGrossMarginRate(request.getTargetGrossMarginRate());
        }
        if (request.getCurrentPrice() != null) {
            recipeCost.setCurrentPrice(request.getCurrentPrice());
        }

        BigDecimal totalCost = calculateTotalIngredientCost(recipe);
        recipeCost.setTotalIngredientCost(totalCost);
        recipeCost.setLastCalculatedAt(LocalDateTime.now());

        BigDecimal marginRate = recipeCost.getTargetGrossMarginRate();
        if (marginRate.compareTo(BigDecimal.ONE) < 0) {
            BigDecimal recommended = totalCost.divide(
                    BigDecimal.ONE.subtract(marginRate), 2, RoundingMode.HALF_UP);
            recipeCost.setRecommendedPrice(recommended);
        }

        RecipeCost saved = recipeCostRepository.save(recipeCost);
        log.info("Recipe cost updated: recipeId={}, by={}", recipeId, currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AffectedRecipeResponse> getAffectedRecipes(Long ingredientId) {
        List<Long> recipeIds = ingredientRepository.findRecipeIdsByIngredientId(ingredientId);

        List<AffectedRecipeResponse> results = new ArrayList<>();
        for (Long recipeId : recipeIds) {
            recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED).ifPresent(recipe -> {
                BigDecimal newCost = calculateTotalIngredientCost(recipe);

                RecipeCost existingCost = recipeCostRepository.findByRecipeId(recipeId).orElse(null);

                BigDecimal previousCost = existingCost != null ? existingCost.getTotalIngredientCost() : BigDecimal.ZERO;
                BigDecimal currentPrice = existingCost != null ? existingCost.getCurrentPrice() : null;
                BigDecimal targetMargin = existingCost != null ? existingCost.getTargetGrossMarginRate()
                        : new BigDecimal("0.7000");

                BigDecimal actualMargin = null;
                boolean belowTarget = false;
                if (currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                    actualMargin = currentPrice.subtract(newCost)
                            .divide(currentPrice, 4, RoundingMode.HALF_UP);
                    belowTarget = actualMargin.compareTo(targetMargin) < 0;
                }

                results.add(AffectedRecipeResponse.builder()
                        .recipeId(recipe.getId())
                        .recipeTitle(recipe.getTitle())
                        .status(recipe.getStatus())
                        .previousCost(previousCost)
                        .newCost(newCost)
                        .currentPrice(currentPrice)
                        .targetGrossMarginRate(targetMargin)
                        .actualGrossMarginRate(actualMargin)
                        .belowTarget(belowTarget)
                        .build());
            });
        }

        return results;
    }

    @Transactional
    public List<RecipeCost> recalculateByIngredient(Long ingredientId, User currentUser) {
        validateCostViewPermission(currentUser);

        List<Long> recipeIds = ingredientRepository.findRecipeIdsByIngredientId(ingredientId);
        List<RecipeCost> recalculated = new ArrayList<>();

        for (Long recipeId : recipeIds) {
            recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED).ifPresent(recipe -> {
                BigDecimal totalCost = calculateTotalIngredientCost(recipe);

                RecipeCost recipeCost = recipeCostRepository.findByRecipeId(recipeId)
                        .orElse(RecipeCost.builder()
                                .recipe(recipe)
                                .build());

                recipeCost.setTotalIngredientCost(totalCost);
                recipeCost.setLastCalculatedAt(LocalDateTime.now());

                BigDecimal marginRate = recipeCost.getTargetGrossMarginRate();
                if (marginRate.compareTo(BigDecimal.ONE) < 0) {
                    BigDecimal recommended = totalCost.divide(
                            BigDecimal.ONE.subtract(marginRate), 2, RoundingMode.HALF_UP);
                    recipeCost.setRecommendedPrice(recommended);
                }

                recalculated.add(recipeCostRepository.save(recipeCost));
            });
        }

        log.info("Recalculated costs for {} recipes affected by ingredientId={}, by={}",
                recalculated.size(), ingredientId, currentUser.getEmail());
        return recalculated;
    }

    private BigDecimal calculateTotalIngredientCost(Recipe recipe) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (RecipeIngredient ri : recipe.getIngredients()) {
            var currentPrice = ingredientPriceRepository.findCurrentPrice(
                    ri.getIngredient().getId(), today);

            if (currentPrice.isPresent() && ri.getQuantity() != null) {
                BigDecimal cost = currentPrice.get().getUnitPrice().multiply(ri.getQuantity());
                total = total.add(cost);
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCostViewPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("原価情報の閲覧権限がありません");
        }
    }

    private void validateCostUpdatePermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("売価の更新権限がありません");
        }
    }
}
