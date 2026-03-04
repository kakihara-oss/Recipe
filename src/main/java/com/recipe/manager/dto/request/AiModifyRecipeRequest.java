package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiModifyRecipeRequest {

    @NotBlank(message = "修正指示は必須です")
    @Size(max = 1000, message = "修正指示は1000文字以内で入力してください")
    private String instruction;

    private CurrentRecipe currentRecipe;

    @Getter
    @Setter
    public static class CurrentRecipe {
        private String title;
        private String description;
        private String category;
        private Integer servings;
        private String concept;
        private String story;
        private List<StepData> cookingSteps;
        private List<IngredientData> ingredients;
    }

    @Getter
    @Setter
    public static class StepData {
        private Integer stepNumber;
        private String description;
        private Integer durationMinutes;
        private String temperature;
        private String tips;
    }

    @Getter
    @Setter
    public static class IngredientData {
        private String name;
        private String quantity;
        private String preparationNote;
    }
}
