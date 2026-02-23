package com.recipe.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExperienceDesignRequest {

    private String targetScene;
    private String emotionalKeyPoints;
    private String specialOccasionSupport;
    private String seasonalPresentation;
    private String sensoryAppeal;
}
