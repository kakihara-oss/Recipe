package com.recipe.manager.dto.response;

import com.recipe.manager.entity.RecipeHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecipeHistoryResponse {

    private final Long id;
    private final String changeType;
    private final String changedFields;
    private final String changedByName;
    private final LocalDateTime changedAt;

    public static RecipeHistoryResponse from(RecipeHistory history) {
        return RecipeHistoryResponse.builder()
                .id(history.getId())
                .changeType(history.getChangeType())
                .changedFields(history.getChangedFields())
                .changedByName(history.getChangedBy().getName())
                .changedAt(history.getChangedAt())
                .build();
    }
}
