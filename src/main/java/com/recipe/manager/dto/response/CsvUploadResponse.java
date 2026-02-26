package com.recipe.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CsvUploadResponse {

    private final int totalRows;
    private final int successRows;
    private final int errorRows;
    private final List<String> errors;
}
