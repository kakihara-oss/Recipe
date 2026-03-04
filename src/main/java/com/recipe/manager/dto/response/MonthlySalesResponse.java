package com.recipe.manager.dto.response;

import com.recipe.manager.entity.MonthlySales;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class MonthlySalesResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private Long recipeId;
    private String recipeTitle;
    private String targetMonth;
    private Integer quantity;
    private BigDecimal salesAmount;

    public static MonthlySalesResponse from(MonthlySales ms) {
        return MonthlySalesResponse.builder()
                .id(ms.getId())
                .storeId(ms.getStore().getId())
                .storeName(ms.getStore().getName())
                .recipeId(ms.getRecipe().getId())
                .recipeTitle(ms.getRecipe().getTitle())
                .targetMonth(ms.getTargetMonth())
                .quantity(ms.getQuantity())
                .salesAmount(ms.getSalesAmount())
                .build();
    }
}
