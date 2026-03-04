package com.recipe.manager.service;

import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeIngredientRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeCostService {

    private final RecipeCostRepository recipeCostRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientPriceRepository ingredientPriceRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    @Transactional
    public RecipeCost calculateCost(Long recipeId) {
        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        BigDecimal totalCost = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ri : recipe.getIngredients()) {
                IngredientPrice currentPrice = ingredientPriceRepository
                        .findCurrentPrice(ri.getIngredient().getId(), today)
                        .orElse(null);

                if (currentPrice != null && ri.getQuantity() != null) {
                    totalCost = totalCost.add(currentPrice.getUnitPrice().multiply(ri.getQuantity()));
                }
            }
        }

        RecipeCost recipeCost = recipeCostRepository.findByRecipeId(recipeId)
                .orElse(RecipeCost.builder().recipe(recipe).build());

        recipeCost.setTotalIngredientCost(totalCost);
        recipeCost.setLastCalculatedAt(LocalDateTime.now());

        if (recipeCost.getTargetMarginRate() != null) {
            BigDecimal marginDivisor = BigDecimal.ONE
                    .subtract(recipeCost.getTargetMarginRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            if (marginDivisor.compareTo(BigDecimal.ZERO) > 0) {
                recipeCost.setRecommendedPrice(
                        totalCost.divide(marginDivisor, 0, RoundingMode.CEILING));
            }
        }

        RecipeCost saved = recipeCostRepository.save(recipeCost);
        log.info("Recipe cost calculated: recipeId={}, totalCost={}", recipeId, totalCost);
        return saved;
    }

    @Transactional(readOnly = true)
    public RecipeCost getRecipeCost(Long recipeId) {
        return recipeCostRepository.findByRecipeId(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("RecipeCost for recipe", recipeId));
    }

    @Transactional
    public RecipeCost updateCostSettings(Long recipeId, UpdateRecipeCostRequest request, User currentUser) {
        validateCostPermission(currentUser);

        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        RecipeCost recipeCost = recipeCostRepository.findByRecipeId(recipeId)
                .orElse(RecipeCost.builder().recipe(recipe).build());

        if (request.getTargetMarginRate() != null) {
            recipeCost.setTargetMarginRate(request.getTargetMarginRate());
        }
        if (request.getCurrentPrice() != null) {
            recipeCost.setCurrentPrice(request.getCurrentPrice());
        }

        if (recipeCost.getTargetMarginRate() != null && recipeCost.getTotalIngredientCost() != null) {
            BigDecimal marginDivisor = BigDecimal.ONE
                    .subtract(recipeCost.getTargetMarginRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            if (marginDivisor.compareTo(BigDecimal.ZERO) > 0) {
                recipeCost.setRecommendedPrice(
                        recipeCost.getTotalIngredientCost().divide(marginDivisor, 0, RoundingMode.CEILING));
            }
        }

        RecipeCost saved = recipeCostRepository.save(recipeCost);
        log.info("Recipe cost settings updated: recipeId={}, by={}", recipeId, currentUser.getEmail());
        return saved;
    }

    @Transactional
    public List<RecipeCost> recalculateAffectedRecipes(Long ingredientId) {
        List<RecipeIngredient> affectedIngredients = recipeIngredientRepository.findByIngredientId(ingredientId);

        List<Long> recipeIds = affectedIngredients.stream()
                .map(ri -> ri.getRecipe().getId())
                .distinct()
                .toList();

        List<RecipeCost> results = new java.util.ArrayList<>();
        for (Long recipeId : recipeIds) {
            results.add(calculateCost(recipeId));
        }

        log.info("Recalculated costs for {} recipes affected by ingredient {}", results.size(), ingredientId);
        return results;
    }

    @Transactional(readOnly = true)
    public List<RecipeCost> getWarningRecipes() {
        return recipeCostRepository.findWarningRecipes();
    }

    private void validateCostPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("原価情報の更新権限がありません");
        }
    }
}
