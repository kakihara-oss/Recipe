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
    private final BigDecimal targetGrossMarginRate;
    private final BigDecimal recommendedPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal actualGrossMarginRate;
    private final boolean belowTarget;
    private final LocalDateTime lastCalculatedAt;

    public static RecipeCostResponse from(RecipeCost cost) {
        BigDecimal actualMargin = null;
        boolean belowTarget = false;

        if (cost.getCurrentPrice() != null && cost.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
            actualMargin = cost.getCurrentPrice().subtract(cost.getTotalIngredientCost())
                    .divide(cost.getCurrentPrice(), 4, java.math.RoundingMode.HALF_UP);
            belowTarget = actualMargin.compareTo(cost.getTargetGrossMarginRate()) < 0;
        }

        return RecipeCostResponse.builder()
                .id(cost.getId())
                .recipeId(cost.getRecipe().getId())
                .recipeTitle(cost.getRecipe().getTitle())
                .totalIngredientCost(cost.getTotalIngredientCost())
                .targetGrossMarginRate(cost.getTargetGrossMarginRate())
                .recommendedPrice(cost.getRecommendedPrice())
                .currentPrice(cost.getCurrentPrice())
                .actualGrossMarginRate(actualMargin)
                .belowTarget(belowTarget)
                .lastCalculatedAt(cost.getLastCalculatedAt())
                .build();
    }
}
