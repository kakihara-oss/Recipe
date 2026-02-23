package com.recipe.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceDesignRequest {

    private String platingInstructions;
    private String serviceMethod;
    private String customerScript;
    private String stagingMethod;
    private String timing;
    private String storytelling;
}
