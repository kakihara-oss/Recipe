package com.recipe.manager.dto.response;

import com.recipe.manager.entity.AiConsultationThread;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiThreadResponse {

    private final Long id;
    private final String theme;
    private final Long recipeId;
    private final String recipeName;
    private final String userName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static AiThreadResponse from(AiConsultationThread thread) {
        return AiThreadResponse.builder()
                .id(thread.getId())
                .theme(thread.getTheme())
                .recipeId(thread.getRecipe() != null ? thread.getRecipe().getId() : null)
                .recipeName(thread.getRecipe() != null ? thread.getRecipe().getTitle() : null)
                .userName(thread.getUser().getName())
                .createdAt(thread.getCreatedAt())
                .updatedAt(thread.getUpdatedAt())
                .build();
    }
}
