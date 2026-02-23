package com.recipe.manager.dto.response;

import com.recipe.manager.entity.CollectionMethod;
import com.recipe.manager.entity.ProductFeedback;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductFeedbackResponse {

    private final Long id;
    private final Long recipeId;
    private final String recipeTitle;
    private final Long storeId;
    private final String storeName;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final Integer satisfactionScore;
    private final Integer emotionScore;
    private final String comment;
    private final CollectionMethod collectionMethod;
    private final String registeredByName;
    private final LocalDateTime createdAt;

    public static ProductFeedbackResponse from(ProductFeedback feedback) {
        return ProductFeedbackResponse.builder()
                .id(feedback.getId())
                .recipeId(feedback.getRecipe().getId())
                .recipeTitle(feedback.getRecipe().getTitle())
                .storeId(feedback.getStore() != null ? feedback.getStore().getId() : null)
                .storeName(feedback.getStore() != null ? feedback.getStore().getName() : null)
                .periodStart(feedback.getPeriodStart())
                .periodEnd(feedback.getPeriodEnd())
                .satisfactionScore(feedback.getSatisfactionScore())
                .emotionScore(feedback.getEmotionScore())
                .comment(feedback.getComment())
                .collectionMethod(feedback.getCollectionMethod())
                .registeredByName(feedback.getRegisteredBy().getName())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
