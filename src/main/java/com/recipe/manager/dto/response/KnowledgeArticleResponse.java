package com.recipe.manager.dto.response;

import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class KnowledgeArticleResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String categoryName;
    private final Long categoryId;
    private final String tags;
    private final String authorName;
    private final Long authorId;
    private final List<RelatedRecipeInfo> relatedRecipes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class RelatedRecipeInfo {
        private final Long id;
        private final String title;
    }

    public static KnowledgeArticleResponse from(KnowledgeArticle article) {
        List<RelatedRecipeInfo> recipeInfos = null;
        if (article.getRelatedRecipes() != null) {
            recipeInfos = article.getRelatedRecipes().stream()
                    .map(r -> RelatedRecipeInfo.builder()
                            .id(r.getId())
                            .title(r.getTitle())
                            .build())
                    .toList();
        }

        return KnowledgeArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .categoryName(article.getCategory().getName())
                .categoryId(article.getCategory().getId())
                .tags(article.getTags())
                .authorName(article.getAuthor().getName())
                .authorId(article.getAuthor().getId())
                .relatedRecipes(recipeInfos)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
