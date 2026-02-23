package com.recipe.manager.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRecipeRequest {

    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @Size(max = 2000, message = "説明は2000文字以内で入力してください")
    private String description;

    private String category;
    private Integer servings;
    private String concept;
    private String story;
}
