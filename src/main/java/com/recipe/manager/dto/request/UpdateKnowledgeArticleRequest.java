package com.recipe.manager.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateKnowledgeArticleRequest {

    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    private String content;

    private Long categoryId;

    private String tags;

    private List<Long> relatedRecipeIds;
}
