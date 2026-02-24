package com.recipe.manager.dto.response;

import com.recipe.manager.entity.MonthlySales;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlySalesResponse {

    private final Long id;
    private final Long storeId;
    private final String storeName;
    private final Long recipeId;
    private final String recipeTitle;
    private final String salesMonth;
    private final Integer quantity;
    private final BigDecimal salesAmount;

    public static MonthlySalesResponse from(MonthlySales sales) {
        return MonthlySalesResponse.builder()
                .id(sales.getId())
                .storeId(sales.getStore().getId())
                .storeName(sales.getStore().getName())
                .recipeId(sales.getRecipe().getId())
                .recipeTitle(sales.getRecipe().getTitle())
                .salesMonth(sales.getSalesMonth())
                .quantity(sales.getQuantity())
                .salesAmount(sales.getSalesAmount())
                .build();
    }
}
