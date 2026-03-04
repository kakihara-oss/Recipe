package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGenerateRecipeRequest {

    @NotBlank(message = "テーマは必須です")
    @Size(max = 200, message = "テーマは200文字以内で入力してください")
    private String theme;
}
