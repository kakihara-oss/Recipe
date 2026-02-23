package com.recipe.manager.dto.request;

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
public class GenerateFeedbackSummaryRequest {

    @NotNull(message = "レシピIDは必須です")
    private Long recipeId;

    @NotNull(message = "集計期間の開始日は必須です")
    private LocalDate periodStart;

    @NotNull(message = "集計期間の終了日は必須です")
    private LocalDate periodEnd;
}
