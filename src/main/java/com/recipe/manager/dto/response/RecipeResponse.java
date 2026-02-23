package com.recipe.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recipe.manager.entity.CookingStep;
import com.recipe.manager.entity.ExperienceDesign;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.ServiceDesign;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipeResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Integer servings;
    private final RecipeStatus status;
    private final String concept;
    private final String story;
    private final CreatedByInfo createdBy;
    private final List<CookingStepInfo> cookingSteps;
    private final List<IngredientInfo> ingredients;
    private final ServiceDesignInfo serviceDesign;
    private final ExperienceDesignInfo experienceDesign;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class CreatedByInfo {
        private final Long id;
        private final String name;
        private final String role;
    }

    @Getter
    @Builder
    public static class CookingStepInfo {
        private final Long id;
        private final Integer stepNumber;
        private final String description;
        private final Integer durationMinutes;
        private final String temperature;
        private final String tips;
    }

    @Getter
    @Builder
    public static class IngredientInfo {
        private final Long id;
        private final Long ingredientId;
        private final String ingredientName;
        private final BigDecimal quantity;
        private final String unit;
        private final String preparationNote;
        private final String substitutes;
    }

    @Getter
    @Builder
    public static class ServiceDesignInfo {
        private final Long id;
        private final String platingInstructions;
        private final String serviceMethod;
        private final String customerScript;
        private final String stagingMethod;
        private final String timing;
        private final String storytelling;
    }

    @Getter
    @Builder
    public static class ExperienceDesignInfo {
        private final Long id;
        private final String targetScene;
        private final String emotionalKeyPoints;
        private final String specialOccasionSupport;
        private final String seasonalPresentation;
        private final String sensoryAppeal;
    }

    public static RecipeResponse from(Recipe recipe) {
        RecipeResponseBuilder builder = RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .category(recipe.getCategory())
                .servings(recipe.getServings())
                .status(recipe.getStatus())
                .concept(recipe.getConcept())
                .story(recipe.getStory())
                .createdBy(CreatedByInfo.builder()
                        .id(recipe.getCreatedBy().getId())
                        .name(recipe.getCreatedBy().getName())
                        .role(recipe.getCreatedBy().getRole().name())
                        .build())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt());

        if (recipe.getCookingSteps() != null) {
            builder.cookingSteps(recipe.getCookingSteps().stream()
                    .map(RecipeResponse::toCookingStepInfo)
                    .toList());
        }

        if (recipe.getIngredients() != null) {
            builder.ingredients(recipe.getIngredients().stream()
                    .map(RecipeResponse::toIngredientInfo)
                    .toList());
        }

        if (recipe.getServiceDesign() != null) {
            builder.serviceDesign(toServiceDesignInfo(recipe.getServiceDesign()));
        }

        if (recipe.getExperienceDesign() != null) {
            builder.experienceDesign(toExperienceDesignInfo(recipe.getExperienceDesign()));
        }

        return builder.build();
    }

    private static CookingStepInfo toCookingStepInfo(CookingStep step) {
        return CookingStepInfo.builder()
                .id(step.getId())
                .stepNumber(step.getStepNumber())
                .description(step.getDescription())
                .durationMinutes(step.getDurationMinutes())
                .temperature(step.getTemperature())
                .tips(step.getTips())
                .build();
    }

    private static IngredientInfo toIngredientInfo(RecipeIngredient ri) {
        return IngredientInfo.builder()
                .id(ri.getId())
                .ingredientId(ri.getIngredient().getId())
                .ingredientName(ri.getIngredient().getName())
                .quantity(ri.getQuantity())
                .unit(ri.getUnit())
                .preparationNote(ri.getPreparationNote())
                .substitutes(ri.getSubstitutes())
                .build();
    }

    private static ServiceDesignInfo toServiceDesignInfo(ServiceDesign sd) {
        return ServiceDesignInfo.builder()
                .id(sd.getId())
                .platingInstructions(sd.getPlatingInstructions())
                .serviceMethod(sd.getServiceMethod())
                .customerScript(sd.getCustomerScript())
                .stagingMethod(sd.getStagingMethod())
                .timing(sd.getTiming())
                .storytelling(sd.getStorytelling())
                .build();
    }

    private static ExperienceDesignInfo toExperienceDesignInfo(ExperienceDesign ed) {
        return ExperienceDesignInfo.builder()
                .id(ed.getId())
                .targetScene(ed.getTargetScene())
                .emotionalKeyPoints(ed.getEmotionalKeyPoints())
                .specialOccasionSupport(ed.getSpecialOccasionSupport())
                .seasonalPresentation(ed.getSeasonalPresentation())
                .sensoryAppeal(ed.getSensoryAppeal())
                .build();
    }
}
