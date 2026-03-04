package com.recipe.manager.dto.response;

import com.recipe.manager.entity.RecipeCost;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RecipeCostResponse {

    private final Long id;
    private final Long recipeId;
    private final String recipeTitle;
    private final BigDecimal totalIngredientCost;
    private final BigDecimal targetMarginRate;
    private final BigDecimal recommendedPrice;
    private final BigDecimal currentPrice;
    private final LocalDateTime lastCalculatedAt;

    public static RecipeCostResponse from(RecipeCost cost) {
        return RecipeCostResponse.builder()
                .id(cost.getId())
                .recipeId(cost.getRecipe().getId())
                .recipeTitle(cost.getRecipe().getTitle())
                .totalIngredientCost(cost.getTotalIngredientCost())
                .targetMarginRate(cost.getTargetMarginRate())
                .recommendedPrice(cost.getRecommendedPrice())
                .currentPrice(cost.getCurrentPrice())
                .lastCalculatedAt(cost.getLastCalculatedAt())
                .build();
    }
}
