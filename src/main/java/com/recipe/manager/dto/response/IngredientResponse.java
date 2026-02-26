package com.recipe.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.SupplyStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngredientResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String standardUnit;
    private final SupplyStatus supplyStatus;
    private final String supplier;
    private final List<PriceInfo> prices;
    private final List<SeasonInfo> seasons;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class PriceInfo {
        private final Long id;
        private final BigDecimal unitPrice;
        private final String pricePerUnit;
        private final LocalDate effectiveFrom;
        private final LocalDate effectiveTo;
    }

    @Getter
    @Builder
    public static class SeasonInfo {
        private final Long id;
        private final Integer month;
        private final String availabilityRank;
        private final String qualityNote;
    }

    public static IngredientResponse from(Ingredient ingredient) {
        IngredientResponseBuilder builder = IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .standardUnit(ingredient.getStandardUnit())
                .supplyStatus(ingredient.getSupplyStatus())
                .supplier(ingredient.getSupplier())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt());

        if (ingredient.getPrices() != null) {
            builder.prices(ingredient.getPrices().stream()
                    .map(IngredientResponse::toPriceInfo)
                    .toList());
        }

        if (ingredient.getSeasons() != null) {
            builder.seasons(ingredient.getSeasons().stream()
                    .map(IngredientResponse::toSeasonInfo)
                    .toList());
        }

        return builder.build();
    }

    private static PriceInfo toPriceInfo(IngredientPrice price) {
        return PriceInfo.builder()
                .id(price.getId())
                .unitPrice(price.getUnitPrice())
                .pricePerUnit(price.getPricePerUnit())
                .effectiveFrom(price.getEffectiveFrom())
                .effectiveTo(price.getEffectiveTo())
                .build();
    }

    private static SeasonInfo toSeasonInfo(IngredientSeason season) {
        return SeasonInfo.builder()
                .id(season.getId())
                .month(season.getMonth())
                .availabilityRank(season.getAvailabilityRank())
                .qualityNote(season.getQualityNote())
                .build();
    }
}
