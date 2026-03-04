package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGenerateFromRecipeRequest {

    @NotNull(message = "レシピIDは必須です")
    private Long recipeId;

    @Size(max = 500, message = "アレンジ指示は500文字以内で入力してください")
    private String arrangementInstruction;
}
