package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAiThreadRequest {

    @NotBlank(message = "相談テーマは必須です")
    @Size(max = 200, message = "相談テーマは200文字以内で入力してください")
    private String theme;

    private Long recipeId;

    @NotBlank(message = "最初のメッセージは必須です")
    private String initialMessage;
}
