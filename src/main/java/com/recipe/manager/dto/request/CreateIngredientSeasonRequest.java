package com.recipe.manager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIngredientSeasonRequest {

    @NotNull(message = "月は必須です")
    @Min(value = 1, message = "月は1〜12の範囲で入力してください")
    @Max(value = 12, message = "月は1〜12の範囲で入力してください")
    private Integer month;

    @NotBlank(message = "入手性ランクは必須です")
    @Size(max = 20, message = "入手性ランクは20文字以内で入力してください")
    private String availabilityRank;

    @Size(max = 500, message = "品質メモは500文字以内で入力してください")
    private String qualityNote;
}
