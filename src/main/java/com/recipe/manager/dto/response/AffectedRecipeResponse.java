package com.recipe.manager.dto.response;

import com.recipe.manager.entity.RecipeStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AffectedRecipeResponse {

    private final Long recipeId;
    private final String recipeTitle;
    private final RecipeStatus status;
    private final BigDecimal previousCost;
    private final BigDecimal newCost;
    private final BigDecimal currentPrice;
    private final BigDecimal targetGrossMarginRate;
    private final BigDecimal actualGrossMarginRate;
    private final boolean belowTarget;
}
