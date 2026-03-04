package com.recipe.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AffectedRecipeResponse {

    private final Long recipeId;
    private final String recipeTitle;
    private final BigDecimal previousCost;
    private final BigDecimal newCost;
}
