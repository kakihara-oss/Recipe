package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientSeasonRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.dto.response.IngredientListResponse;
import com.recipe.manager.dto.response.IngredientPriceResponse;
import com.recipe.manager.dto.response.IngredientResponse;
import com.recipe.manager.dto.response.IngredientSeasonResponse;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.IngredientService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ingredient ingredient = ingredientService.createIngredient(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailResponse(ingredient));
    }

    @GetMapping
    public ResponseEntity<Page<IngredientListResponse>> listIngredients(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) SupplyStatus supplyStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        Page<Ingredient> ingredients = ingredientService.listIngredients(category, supplyStatus, keyword, pageable);
        return ResponseEntity.ok(ingredients.map(i -> {
            IngredientPrice currentPrice = ingredientService.getCurrentPrice(i.getId());
            return IngredientListResponse.from(i, currentPrice != null ? currentPrice.getUnitPrice() : null);
        }));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getIngredient(@PathVariable Long id) {
        Ingredient ingredient = ingredientService.getIngredientById(id);
        return ResponseEntity.ok(toDetailResponse(ingredient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ingredient ingredient = ingredientService.updateIngredient(id, request, currentUser);
        return ResponseEntity.ok(toDetailResponse(ingredient));
    }

    @PutMapping("/{id}/supply-status")
    public ResponseEntity<IngredientResponse> updateSupplyStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        SupplyStatus newStatus = SupplyStatus.valueOf(body.get("supplyStatus"));
        Ingredient ingredient = ingredientService.updateSupplyStatus(id, newStatus, currentUser);
        return ResponseEntity.ok(toDetailResponse(ingredient));
    }

    @PostMapping("/{id}/prices")
    public ResponseEntity<IngredientPriceResponse> addPrice(
            @PathVariable Long id,
            @Valid @RequestBody CreateIngredientPriceRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        IngredientPrice price = ingredientService.addPrice(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(IngredientPriceResponse.from(price));
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<IngredientPriceResponse>> getPriceHistory(@PathVariable Long id) {
        List<IngredientPriceResponse> prices = ingredientService.getPriceHistory(id).stream()
                .map(IngredientPriceResponse::from)
                .toList();
        return ResponseEntity.ok(prices);
    }

    @PutMapping("/{id}/seasons")
    public ResponseEntity<List<IngredientSeasonResponse>> updateSeasons(
            @PathVariable Long id,
            @Valid @RequestBody List<UpdateIngredientSeasonRequest> requests,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<IngredientSeason> seasons = ingredientService.updateSeasons(id, requests, currentUser);
        return ResponseEntity.ok(seasons.stream().map(IngredientSeasonResponse::from).toList());
    }

    @GetMapping("/{id}/seasons")
    public ResponseEntity<List<IngredientSeasonResponse>> getSeasons(@PathVariable Long id) {
        List<IngredientSeasonResponse> seasons = ingredientService.getSeasons(id).stream()
                .map(IngredientSeasonResponse::from)
                .toList();
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/{id}/affected-recipes")
    public ResponseEntity<List<AffectedRecipeResponse>> getAffectedRecipes(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getAffectedRecipes(id));
    }

    private IngredientResponse toDetailResponse(Ingredient ingredient) {
        IngredientPrice currentPrice = ingredientService.getCurrentPrice(ingredient.getId());
        List<IngredientPrice> prices = ingredientService.getPriceHistory(ingredient.getId());
        List<IngredientSeason> seasons = ingredientService.getSeasons(ingredient.getId());
        return IngredientResponse.from(ingredient, currentPrice, prices, seasons);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
