package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientSeasonRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.IngredientSeasonRepository;
import com.recipe.manager.repository.RecipeIngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientPriceRepository priceRepository;
    private final IngredientSeasonRepository seasonRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    @Transactional
    public Ingredient createIngredient(CreateIngredientRequest request, User currentUser) {
        validateIngredientPermission(currentUser);

        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .category(request.getCategory())
                .standardUnit(request.getStandardUnit())
                .seasonalFlag(request.getSeasonalFlag() != null ? request.getSeasonalFlag() : false)
                .supplyStatus(request.getSupplyStatus() != null ? request.getSupplyStatus() : SupplyStatus.AVAILABLE)
                .supplier(request.getSupplier())
                .build();

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient created: id={}, name={}, by={}", saved.getId(), saved.getName(), currentUser.getEmail());
        return saved;
    }

    @Transactional
    public Ingredient updateIngredient(Long id, UpdateIngredientRequest request, User currentUser) {
        validateIngredientPermission(currentUser);

        Ingredient ingredient = getIngredientById(id);

        if (request.getName() != null) {
            ingredient.setName(request.getName());
        }
        if (request.getCategory() != null) {
            ingredient.setCategory(request.getCategory());
        }
        if (request.getStandardUnit() != null) {
            ingredient.setStandardUnit(request.getStandardUnit());
        }
        if (request.getSeasonalFlag() != null) {
            ingredient.setSeasonalFlag(request.getSeasonalFlag());
        }
        if (request.getSupplyStatus() != null) {
            ingredient.setSupplyStatus(request.getSupplyStatus());
        }
        if (request.getSupplier() != null) {
            ingredient.setSupplier(request.getSupplier());
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient updated: id={}, by={}", id, currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", id));
    }

    @Transactional(readOnly = true)
    public Page<Ingredient> listIngredients(String category, SupplyStatus supplyStatus, String keyword, Pageable pageable) {
        return ingredientRepository.findByFilters(category, supplyStatus, keyword, pageable);
    }

    @Transactional
    public Ingredient updateSupplyStatus(Long id, SupplyStatus newStatus, User currentUser) {
        validateIngredientPermission(currentUser);

        Ingredient ingredient = getIngredientById(id);
        ingredient.setSupplyStatus(newStatus);

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient supply status updated: id={}, status={}, by={}", id, newStatus, currentUser.getEmail());
        return saved;
    }

    @Transactional
    public IngredientPrice addPrice(Long ingredientId, CreateIngredientPriceRequest request, User currentUser) {
        validateIngredientPermission(currentUser);

        Ingredient ingredient = getIngredientById(ingredientId);

        IngredientPrice price = IngredientPrice.builder()
                .ingredient(ingredient)
                .unitPrice(request.getUnitPrice())
                .pricePerUnit(request.getPricePerUnit())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();

        IngredientPrice saved = priceRepository.save(price);
        log.info("Ingredient price added: ingredientId={}, unitPrice={}, by={}", ingredientId, request.getUnitPrice(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<IngredientPrice> getPriceHistory(Long ingredientId) {
        getIngredientById(ingredientId);
        return priceRepository.findByIngredientIdOrderByEffectiveFromDesc(ingredientId);
    }

    @Transactional(readOnly = true)
    public IngredientPrice getCurrentPrice(Long ingredientId) {
        return priceRepository.findCurrentPrice(ingredientId, LocalDate.now()).orElse(null);
    }

    @Transactional
    public List<IngredientSeason> updateSeasons(Long ingredientId, List<UpdateIngredientSeasonRequest> requests, User currentUser) {
        validateIngredientPermission(currentUser);

        Ingredient ingredient = getIngredientById(ingredientId);
        seasonRepository.deleteByIngredientId(ingredientId);

        List<IngredientSeason> seasons = requests.stream()
                .map(req -> IngredientSeason.builder()
                        .ingredient(ingredient)
                        .month(req.getMonth())
                        .availabilityRank(req.getAvailabilityRank())
                        .qualityNote(req.getQualityNote())
                        .build())
                .toList();

        List<IngredientSeason> saved = seasonRepository.saveAll(seasons);
        log.info("Ingredient seasons updated: ingredientId={}, count={}, by={}", ingredientId, saved.size(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<IngredientSeason> getSeasons(Long ingredientId) {
        getIngredientById(ingredientId);
        return seasonRepository.findByIngredientIdOrderByMonth(ingredientId);
    }

    @Transactional(readOnly = true)
    public List<AffectedRecipeResponse> getAffectedRecipes(Long ingredientId) {
        getIngredientById(ingredientId);
        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByIngredientId(ingredientId);

        return recipeIngredients.stream()
                .map(ri -> AffectedRecipeResponse.builder()
                        .recipeId(ri.getRecipe().getId())
                        .recipeTitle(ri.getRecipe().getTitle())
                        .build())
                .distinct()
                .toList();
    }

    private void validateIngredientPermission(User user) {
        Role role = user.getRole();
        if (role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("食材マスタの操作権限がありません");
        }
    }
}
