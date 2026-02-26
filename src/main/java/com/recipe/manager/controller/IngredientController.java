package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.CreateIngredientSeasonRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.dto.response.IngredientListResponse;
import com.recipe.manager.dto.response.IngredientResponse;
import com.recipe.manager.dto.response.RecipeCostResponse;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.IngredientService;
import com.recipe.manager.service.RecipeCostService;
import com.recipe.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final RecipeCostService recipeCostService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ingredient ingredient = ingredientService.createIngredient(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(IngredientResponse.from(ingredient));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getIngredient(@PathVariable Long id) {
        Ingredient ingredient = ingredientService.getIngredientById(id);
        return ResponseEntity.ok(IngredientResponse.from(ingredient));
    }

    @GetMapping
    public ResponseEntity<Page<IngredientListResponse>> listIngredients(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) SupplyStatus supplyStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        Page<Ingredient> ingredients = ingredientService.listIngredients(category, supplyStatus, pageable);
        return ResponseEntity.ok(ingredients.map(IngredientListResponse::from));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ingredient ingredient = ingredientService.updateIngredient(id, request, currentUser);
        return ResponseEntity.ok(IngredientResponse.from(ingredient));
    }

    @PutMapping("/{id}/supply-status")
    public ResponseEntity<IngredientResponse> updateSupplyStatus(
            @PathVariable Long id,
            @RequestParam SupplyStatus status,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ingredient ingredient = ingredientService.updateSupplyStatus(id, status, currentUser);
        return ResponseEntity.ok(IngredientResponse.from(ingredient));
    }

    @PostMapping("/{id}/prices")
    public ResponseEntity<IngredientResponse.PriceInfo> addPrice(
            @PathVariable Long id,
            @Valid @RequestBody CreateIngredientPriceRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        IngredientPrice price = ingredientService.addPrice(id, request, currentUser);
        IngredientResponse.PriceInfo priceInfo = IngredientResponse.PriceInfo.builder()
                .id(price.getId())
                .unitPrice(price.getUnitPrice())
                .pricePerUnit(price.getPricePerUnit())
                .effectiveFrom(price.getEffectiveFrom())
                .effectiveTo(price.getEffectiveTo())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(priceInfo);
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<IngredientResponse.PriceInfo>> getPriceHistory(@PathVariable Long id) {
        List<IngredientResponse.PriceInfo> prices = ingredientService.getPriceHistory(id).stream()
                .map(p -> IngredientResponse.PriceInfo.builder()
                        .id(p.getId())
                        .unitPrice(p.getUnitPrice())
                        .pricePerUnit(p.getPricePerUnit())
                        .effectiveFrom(p.getEffectiveFrom())
                        .effectiveTo(p.getEffectiveTo())
                        .build())
                .toList();
        return ResponseEntity.ok(prices);
    }

    @PostMapping("/{id}/seasons")
    public ResponseEntity<IngredientResponse.SeasonInfo> addSeason(
            @PathVariable Long id,
            @Valid @RequestBody CreateIngredientSeasonRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        IngredientSeason season = ingredientService.addSeason(id, request, currentUser);
        IngredientResponse.SeasonInfo seasonInfo = IngredientResponse.SeasonInfo.builder()
                .id(season.getId())
                .month(season.getMonth())
                .availabilityRank(season.getAvailabilityRank())
                .qualityNote(season.getQualityNote())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(seasonInfo);
    }

    @GetMapping("/{id}/seasons")
    public ResponseEntity<List<IngredientResponse.SeasonInfo>> getSeasons(@PathVariable Long id) {
        List<IngredientResponse.SeasonInfo> seasons = ingredientService.getSeasons(id).stream()
                .map(s -> IngredientResponse.SeasonInfo.builder()
                        .id(s.getId())
                        .month(s.getMonth())
                        .availabilityRank(s.getAvailabilityRank())
                        .qualityNote(s.getQualityNote())
                        .build())
                .toList();
        return ResponseEntity.ok(seasons);
    }

    @DeleteMapping("/{id}/seasons/{seasonId}")
    public ResponseEntity<Void> deleteSeason(
            @PathVariable Long id,
            @PathVariable Long seasonId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        ingredientService.deleteSeason(id, seasonId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/affected-recipes")
    public ResponseEntity<List<AffectedRecipeResponse>> getAffectedRecipes(@PathVariable Long id) {
        List<AffectedRecipeResponse> affected = recipeCostService.getAffectedRecipes(id);
        return ResponseEntity.ok(affected);
    }

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<List<RecipeCostResponse>> recalculateByIngredient(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<RecipeCostResponse> results = recipeCostService.recalculateByIngredient(id, currentUser).stream()
                .map(RecipeCostResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
