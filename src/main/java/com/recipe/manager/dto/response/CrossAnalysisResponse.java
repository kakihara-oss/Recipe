package com.recipe.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CrossAnalysisResponse {

    private final Long recipeId;
    private final String recipeTitle;
    private final BigDecimal avgSatisfaction;
    private final BigDecimal avgEmotion;
    private final BigDecimal totalSalesAmount;
    private final Integer totalQuantity;
    private final BigDecimal totalIngredientCost;
    private final BigDecimal theoreticalFoodCostRate;
    private final String insight;
}
