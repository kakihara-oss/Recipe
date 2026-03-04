package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiImproveFieldRequest {

    @NotBlank(message = "フィールド名は必須です")
    private String fieldName;

    private String currentValue;

    private String recipeContext;
}
