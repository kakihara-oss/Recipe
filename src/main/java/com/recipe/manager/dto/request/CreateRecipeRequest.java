package com.recipe.manager.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRecipeRequest {

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @Size(max = 2000, message = "説明は2000文字以内で入力してください")
    private String description;

    private String category;
    private Integer servings;
    private String concept;
    private String story;

    @Valid
    private List<CookingStepInput> cookingSteps;

    @Valid
    private List<IngredientInput> ingredients;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CookingStepInput {
        private Integer stepNumber;
        @NotBlank(message = "手順の説明は必須です")
        private String description;
        private Integer durationMinutes;
        private String temperature;
        private String tips;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientInput {
        private Long ingredientId;
        private BigDecimal quantity;
        private String unit;
        private String preparationNote;
        private String substitutes;
    }
}
