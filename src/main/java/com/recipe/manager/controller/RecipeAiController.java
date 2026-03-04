package com.recipe.manager.controller;

import com.recipe.manager.dto.request.AiGenerateFromRecipeRequest;
import com.recipe.manager.dto.request.AiGenerateRecipeRequest;
import com.recipe.manager.dto.request.AiImproveFieldRequest;
import com.recipe.manager.dto.request.AiModifyRecipeRequest;
import com.recipe.manager.dto.response.AiImproveFieldResponse;
import com.recipe.manager.dto.response.AiRecipeDraftResponse;
import com.recipe.manager.service.RecipeAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes/ai")
@RequiredArgsConstructor
public class RecipeAiController {

    private final RecipeAiService recipeAiService;

    @PostMapping("/generate")
    public ResponseEntity<AiRecipeDraftResponse> generateFromTheme(
            @Valid @RequestBody AiGenerateRecipeRequest request) {
        AiRecipeDraftResponse response = recipeAiService.generateFromTheme(request.getTheme());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-from-recipe")
    public ResponseEntity<AiRecipeDraftResponse> generateFromRecipe(
            @Valid @RequestBody AiGenerateFromRecipeRequest request) {
        AiRecipeDraftResponse response = recipeAiService.generateFromRecipe(
                request.getRecipeId(), request.getArrangementInstruction());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/improve-field")
    public ResponseEntity<AiImproveFieldResponse> improveField(
            @Valid @RequestBody AiImproveFieldRequest request) {
        AiImproveFieldResponse response = recipeAiService.improveField(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/modify")
    public ResponseEntity<AiRecipeDraftResponse> modifyRecipe(
            @Valid @RequestBody AiModifyRecipeRequest request) {
        AiRecipeDraftResponse response = recipeAiService.modifyRecipe(request);
        return ResponseEntity.ok(response);
    }
}
