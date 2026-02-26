package com.recipe.manager.controller;

import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.dto.response.RecipeCostResponse;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.RecipeCostService;
import com.recipe.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes/{recipeId}/cost")
@RequiredArgsConstructor
public class RecipeCostController {

    private final RecipeCostService recipeCostService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<RecipeCostResponse> getRecipeCost(
            @PathVariable Long recipeId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        RecipeCost cost = recipeCostService.getRecipeCost(recipeId, currentUser);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    @PostMapping("/calculate")
    public ResponseEntity<RecipeCostResponse> calculateCost(
            @PathVariable Long recipeId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        RecipeCost cost = recipeCostService.calculateAndSave(recipeId, currentUser);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    @PutMapping
    public ResponseEntity<RecipeCostResponse> updateRecipeCost(
            @PathVariable Long recipeId,
            @Valid @RequestBody UpdateRecipeCostRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        RecipeCost cost = recipeCostService.updateRecipeCost(recipeId, request, currentUser);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
