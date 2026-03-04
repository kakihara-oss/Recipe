package com.recipe.manager.dto.response;

import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.SupplyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class IngredientResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String standardUnit;
    private final Boolean seasonalFlag;
    private final SupplyStatus supplyStatus;
    private final String supplier;
    private final String imageUrl;
    private final IngredientPriceResponse currentPrice;
    private final List<IngredientPriceResponse> prices;
    private final List<IngredientSeasonResponse> seasons;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static IngredientResponse from(
            Ingredient ingredient,
            IngredientPrice currentPrice,
            List<IngredientPrice> prices,
            List<IngredientSeason> seasons) {
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .standardUnit(ingredient.getStandardUnit())
                .seasonalFlag(ingredient.getSeasonalFlag())
                .supplyStatus(ingredient.getSupplyStatus())
                .supplier(ingredient.getSupplier())
                .imageUrl(ingredient.getImageUrl())
                .currentPrice(currentPrice != null ? IngredientPriceResponse.from(currentPrice) : null)
                .prices(prices.stream().map(IngredientPriceResponse::from).toList())
                .seasons(seasons.stream().map(IngredientSeasonResponse::from).toList())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .build();
    }
}
