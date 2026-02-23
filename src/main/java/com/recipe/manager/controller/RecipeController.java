package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateRecipeRequest;
import com.recipe.manager.dto.request.UpdateExperienceDesignRequest;
import com.recipe.manager.dto.request.UpdateRecipeRequest;
import com.recipe.manager.dto.request.UpdateServiceDesignRequest;
import com.recipe.manager.dto.request.UpdateStatusRequest;
import com.recipe.manager.dto.response.RecipeHistoryResponse;
import com.recipe.manager.dto.response.RecipeListResponse;
import com.recipe.manager.dto.response.RecipeResponse;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.RecipeService;
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
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody CreateRecipeRequest request,
                                                        Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Recipe recipe = recipeService.createRecipe(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeResponse.from(recipe));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    @GetMapping
    public ResponseEntity<Page<RecipeListResponse>> listRecipes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) RecipeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {

        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Recipe> recipes = recipeService.listRecipes(category, status, pageable);
        Page<RecipeListResponse> response = recipes.map(RecipeListResponse::from);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateRecipeRequest request,
                                                        Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Recipe recipe = recipeService.updateRecipe(id, request, currentUser);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    @PutMapping("/{id}/service-design")
    public ResponseEntity<RecipeResponse> updateServiceDesign(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateServiceDesignRequest request,
                                                               Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Recipe recipe = recipeService.updateServiceDesign(id, request, currentUser);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    @PutMapping("/{id}/experience-design")
    public ResponseEntity<RecipeResponse> updateExperienceDesign(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateExperienceDesignRequest request,
                                                                  Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Recipe recipe = recipeService.updateExperienceDesign(id, request, currentUser);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RecipeResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateStatusRequest request,
                                                        Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Recipe recipe = recipeService.updateStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(RecipeResponse.from(recipe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id,
                                              Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        recipeService.deleteRecipe(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<RecipeHistoryResponse>> getRecipeHistory(@PathVariable Long id) {
        List<RecipeHistoryResponse> history = recipeService.getRecipeHistory(id).stream()
                .map(RecipeHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(history);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
