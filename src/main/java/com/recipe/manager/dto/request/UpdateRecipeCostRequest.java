package com.recipe.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRecipeCostRequest {

    private BigDecimal targetMarginRate;

    private BigDecimal currentPrice;
}
