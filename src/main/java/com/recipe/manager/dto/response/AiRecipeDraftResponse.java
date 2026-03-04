package com.recipe.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecipeDraftResponse {

    private String title;
    private String description;
    private String category;
    private Integer servings;
    private String concept;
    private String story;
    private List<StepDraft> cookingSteps;
    private List<IngredientDraft> ingredients;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepDraft {
        private Integer stepNumber;
        private String description;
        private Integer durationMinutes;
        private String temperature;
        private String tips;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngredientDraft {
        private String name;
        private String quantity;
        private String preparationNote;
    }
}
