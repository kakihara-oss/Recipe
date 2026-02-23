package com.recipe.manager.dto.request;

import com.recipe.manager.entity.CollectionMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductFeedbackRequest {

    @NotNull(message = "レシピIDは必須です")
    private Long recipeId;

    private Long storeId;

    @NotNull(message = "評価期間の開始日は必須です")
    private LocalDate periodStart;

    @NotNull(message = "評価期間の終了日は必須です")
    private LocalDate periodEnd;

    @NotNull(message = "満足度スコアは必須です")
    @Min(value = 1, message = "満足度スコアは1以上で入力してください")
    @Max(value = 5, message = "満足度スコアは5以下で入力してください")
    private Integer satisfactionScore;

    @Min(value = 1, message = "感動度スコアは1以上で入力してください")
    @Max(value = 5, message = "感動度スコアは5以下で入力してください")
    private Integer emotionScore;

    private String comment;

    @NotNull(message = "収集方法は必須です")
    private CollectionMethod collectionMethod;
}
