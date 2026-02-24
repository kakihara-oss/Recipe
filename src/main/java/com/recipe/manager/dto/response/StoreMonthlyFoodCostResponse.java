package com.recipe.manager.dto.response;

import com.recipe.manager.entity.StoreMonthlyFoodCost;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StoreMonthlyFoodCostResponse {

    private final Long id;
    private final Long storeId;
    private final String storeName;
    private final String salesMonth;
    private final BigDecimal theoreticalFoodCost;
    private final BigDecimal totalSales;
    private final BigDecimal theoreticalFoodCostRate;
    private final LocalDateTime calculatedAt;

    public static StoreMonthlyFoodCostResponse from(StoreMonthlyFoodCost cost) {
        return StoreMonthlyFoodCostResponse.builder()
                .id(cost.getId())
                .storeId(cost.getStore().getId())
                .storeName(cost.getStore().getName())
                .salesMonth(cost.getSalesMonth())
                .theoreticalFoodCost(cost.getTheoreticalFoodCost())
                .totalSales(cost.getTotalSales())
                .theoreticalFoodCostRate(cost.getTheoreticalFoodCostRate())
                .calculatedAt(cost.getCalculatedAt())
                .build();
    }
}
