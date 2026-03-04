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

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeCostController {

    private final RecipeCostService recipeCostService;
    private final UserService userService;

    @GetMapping("/{recipeId}/cost")
    public ResponseEntity<RecipeCostResponse> getRecipeCost(@PathVariable Long recipeId) {
        RecipeCost cost = recipeCostService.getRecipeCost(recipeId);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    @PostMapping("/{recipeId}/cost/calculate")
    public ResponseEntity<RecipeCostResponse> calculateCost(@PathVariable Long recipeId) {
        RecipeCost cost = recipeCostService.calculateCost(recipeId);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    @PutMapping("/{recipeId}/cost")
    public ResponseEntity<RecipeCostResponse> updateCostSettings(
            @PathVariable Long recipeId,
            @Valid @RequestBody UpdateRecipeCostRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        RecipeCost cost = recipeCostService.updateCostSettings(recipeId, request, currentUser);
        return ResponseEntity.ok(RecipeCostResponse.from(cost));
    }

    @GetMapping("/costs/warnings")
    public ResponseEntity<List<RecipeCostResponse>> getWarningRecipes() {
        List<RecipeCostResponse> warnings = recipeCostService.getWarningRecipes().stream()
                .map(RecipeCostResponse::from)
                .toList();
        return ResponseEntity.ok(warnings);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
