package com.recipe.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CsvUploadResponse {

    private int importedCount;
    private int skippedCount;
    private List<String> errors;
}
