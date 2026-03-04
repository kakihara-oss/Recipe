package com.recipe.manager.dto.response;

import com.recipe.manager.entity.IngredientSeason;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngredientSeasonResponse {

    private final Long id;
    private final Integer month;
    private final String availabilityRank;
    private final String qualityNote;

    public static IngredientSeasonResponse from(IngredientSeason season) {
        return IngredientSeasonResponse.builder()
                .id(season.getId())
                .month(season.getMonth())
                .availabilityRank(season.getAvailabilityRank())
                .qualityNote(season.getQualityNote())
                .build();
    }
}
