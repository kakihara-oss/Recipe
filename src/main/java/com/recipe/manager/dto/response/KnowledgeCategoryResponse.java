package com.recipe.manager.dto.response;

import com.recipe.manager.entity.KnowledgeCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KnowledgeCategoryResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final Integer sortOrder;

    public static KnowledgeCategoryResponse from(KnowledgeCategory category) {
        return KnowledgeCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .build();
    }
}
