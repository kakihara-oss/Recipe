package com.recipe.manager.dto.response;

import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecipeListResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Integer servings;
    private final RecipeStatus status;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static RecipeListResponse from(Recipe recipe) {
        return RecipeListResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .category(recipe.getCategory())
                .servings(recipe.getServings())
                .status(recipe.getStatus())
                .createdByName(recipe.getCreatedBy().getName())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}
