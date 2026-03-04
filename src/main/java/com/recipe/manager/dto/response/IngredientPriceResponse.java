package com.recipe.manager.dto.response;

import com.recipe.manager.entity.IngredientPrice;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class IngredientPriceResponse {

    private final Long id;
    private final BigDecimal unitPrice;
    private final String pricePerUnit;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    public static IngredientPriceResponse from(IngredientPrice price) {
        return IngredientPriceResponse.builder()
                .id(price.getId())
                .unitPrice(price.getUnitPrice())
                .pricePerUnit(price.getPricePerUnit())
                .effectiveFrom(price.getEffectiveFrom())
                .effectiveTo(price.getEffectiveTo())
                .build();
    }
}
