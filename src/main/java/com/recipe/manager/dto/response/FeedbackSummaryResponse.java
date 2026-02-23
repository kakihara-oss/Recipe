package com.recipe.manager.dto.response;

import com.recipe.manager.entity.FeedbackSummary;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackSummaryResponse {

    private final Long id;
    private final Long recipeId;
    private final String recipeTitle;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal avgSatisfaction;
    private final BigDecimal avgEmotion;
    private final Integer feedbackCount;
    private final String mainCommentTrend;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static FeedbackSummaryResponse from(FeedbackSummary summary) {
        return FeedbackSummaryResponse.builder()
                .id(summary.getId())
                .recipeId(summary.getRecipe().getId())
                .recipeTitle(summary.getRecipe().getTitle())
                .periodStart(summary.getPeriodStart())
                .periodEnd(summary.getPeriodEnd())
                .avgSatisfaction(summary.getAvgSatisfaction())
                .avgEmotion(summary.getAvgEmotion())
                .feedbackCount(summary.getFeedbackCount())
                .mainCommentTrend(summary.getMainCommentTrend())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }
}
