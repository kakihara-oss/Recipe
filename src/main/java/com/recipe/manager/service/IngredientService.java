package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.CreateIngredientSeasonRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.IngredientSeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientPriceRepository ingredientPriceRepository;
    private final IngredientSeasonRepository ingredientSeasonRepository;

    @Transactional
    public Ingredient createIngredient(CreateIngredientRequest request, User currentUser) {
        validateIngredientManagePermission(currentUser);

        ingredientRepository.findByName(request.getName()).ifPresent(existing -> {
            throw new BusinessLogicException("同名の食材が既に登録されています: " + request.getName());
        });

        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .category(request.getCategory())
                .standardUnit(request.getStandardUnit())
                .supplyStatus(request.getSupplyStatus() != null ? request.getSupplyStatus() : SupplyStatus.AVAILABLE)
                .supplier(request.getSupplier())
                .build();

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient created: id={}, name={}, by={}", saved.getId(), saved.getName(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", id));
    }

    @Transactional(readOnly = true)
    public Page<Ingredient> listIngredients(String category, SupplyStatus supplyStatus, Pageable pageable) {
        if (category != null && supplyStatus != null) {
            return ingredientRepository.findByCategoryAndSupplyStatus(category, supplyStatus, pageable);
        }
        if (category != null) {
            return ingredientRepository.findByCategory(category, pageable);
        }
        if (supplyStatus != null) {
            return ingredientRepository.findBySupplyStatus(supplyStatus, pageable);
        }
        return ingredientRepository.findAll(pageable);
    }

    @Transactional
    public Ingredient updateIngredient(Long id, UpdateIngredientRequest request, User currentUser) {
        validateIngredientManagePermission(currentUser);

        Ingredient ingredient = getIngredientById(id);

        if (request.getName() != null) {
            ingredientRepository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessLogicException("同名の食材が既に登録されています: " + request.getName());
                }
            });
            ingredient.setName(request.getName());
        }
        if (request.getCategory() != null) {
            ingredient.setCategory(request.getCategory());
        }
        if (request.getStandardUnit() != null) {
            ingredient.setStandardUnit(request.getStandardUnit());
        }
        if (request.getSupplyStatus() != null) {
            ingredient.setSupplyStatus(request.getSupplyStatus());
        }
        if (request.getSupplier() != null) {
            ingredient.setSupplier(request.getSupplier());
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient updated: id={}, name={}, by={}", saved.getId(), saved.getName(), currentUser.getEmail());
        return saved;
    }

    @Transactional
    public Ingredient updateSupplyStatus(Long id, SupplyStatus newStatus, User currentUser) {
        validateIngredientManagePermission(currentUser);

        Ingredient ingredient = getIngredientById(id);
        SupplyStatus oldStatus = ingredient.getSupplyStatus();
        ingredient.setSupplyStatus(newStatus);

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("Ingredient supply status changed: id={}, {} -> {}, by={}",
                id, oldStatus, newStatus, currentUser.getEmail());
        return saved;
    }

    @Transactional
    public IngredientPrice addPrice(Long ingredientId, CreateIngredientPriceRequest request, User currentUser) {
        validateIngredientManagePermission(currentUser);

        Ingredient ingredient = getIngredientById(ingredientId);

        if (request.getEffectiveTo() != null && request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new BusinessLogicException("有効開始日は有効終了日より前である必要があります");
        }

        IngredientPrice price = IngredientPrice.builder()
                .ingredient(ingredient)
                .unitPrice(request.getUnitPrice())
                .pricePerUnit(request.getPricePerUnit())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();

        IngredientPrice saved = ingredientPriceRepository.save(price);
        log.info("Ingredient price added: ingredientId={}, unitPrice={}, from={}, by={}",
                ingredientId, saved.getUnitPrice(), saved.getEffectiveFrom(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<IngredientPrice> getPriceHistory(Long ingredientId) {
        getIngredientById(ingredientId);
        return ingredientPriceRepository.findByIngredientIdOrderByEffectiveFromDesc(ingredientId);
    }

    @Transactional
    public IngredientSeason addSeason(Long ingredientId, CreateIngredientSeasonRequest request, User currentUser) {
        validateIngredientManagePermission(currentUser);

        Ingredient ingredient = getIngredientById(ingredientId);

        ingredientSeasonRepository.findByIngredientIdAndMonth(ingredientId, request.getMonth())
                .ifPresent(existing -> {
                    throw new BusinessLogicException(
                            String.format("食材ID=%dの%d月の旬情報は既に登録されています", ingredientId, request.getMonth()));
                });

        IngredientSeason season = IngredientSeason.builder()
                .ingredient(ingredient)
                .month(request.getMonth())
                .availabilityRank(request.getAvailabilityRank())
                .qualityNote(request.getQualityNote())
                .build();

        IngredientSeason saved = ingredientSeasonRepository.save(season);
        log.info("Ingredient season added: ingredientId={}, month={}, by={}",
                ingredientId, saved.getMonth(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<IngredientSeason> getSeasons(Long ingredientId) {
        getIngredientById(ingredientId);
        return ingredientSeasonRepository.findByIngredientIdOrderByMonthAsc(ingredientId);
    }

    @Transactional
    public void deleteSeason(Long ingredientId, Long seasonId, User currentUser) {
        validateIngredientManagePermission(currentUser);
        getIngredientById(ingredientId);

        IngredientSeason season = ingredientSeasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("IngredientSeason", seasonId));

        if (!season.getIngredient().getId().equals(ingredientId)) {
            throw new BusinessLogicException("指定された旬情報はこの食材に属していません");
        }

        ingredientSeasonRepository.delete(season);
        log.info("Ingredient season deleted: ingredientId={}, seasonId={}, by={}",
                ingredientId, seasonId, currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Long> findRecipeIdsByIngredientId(Long ingredientId) {
        getIngredientById(ingredientId);
        return ingredientRepository.findRecipeIdsByIngredientId(ingredientId);
    }

    private void validateIngredientManagePermission(User user) {
        Role role = user.getRole();
        if (role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("食材マスタの管理権限がありません");
        }
    }
}
