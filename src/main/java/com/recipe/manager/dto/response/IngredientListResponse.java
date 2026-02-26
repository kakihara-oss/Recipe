package com.recipe.manager.dto.response;

import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.SupplyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IngredientListResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String standardUnit;
    private final SupplyStatus supplyStatus;
    private final String supplier;
    private final LocalDateTime updatedAt;

    public static IngredientListResponse from(Ingredient ingredient) {
        return IngredientListResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .standardUnit(ingredient.getStandardUnit())
                .supplyStatus(ingredient.getSupplyStatus())
                .supplier(ingredient.getSupplier())
                .updatedAt(ingredient.getUpdatedAt())
                .build();
    }
}
