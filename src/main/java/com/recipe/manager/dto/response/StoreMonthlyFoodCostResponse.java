package com.recipe.manager.dto.response;

import com.recipe.manager.entity.StoreMonthlyFoodCost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class StoreMonthlyFoodCostResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String targetMonth;
    private BigDecimal theoreticalFoodCost;
    private BigDecimal totalSales;
    private BigDecimal theoreticalFoodCostRate;

    public static StoreMonthlyFoodCostResponse from(StoreMonthlyFoodCost fc) {
        return StoreMonthlyFoodCostResponse.builder()
                .id(fc.getId())
                .storeId(fc.getStore().getId())
                .storeName(fc.getStore().getName())
                .targetMonth(fc.getTargetMonth())
                .theoreticalFoodCost(fc.getTheoreticalFoodCost())
                .totalSales(fc.getTotalSales())
                .theoreticalFoodCostRate(fc.getTheoreticalFoodCostRate())
                .build();
    }
}
