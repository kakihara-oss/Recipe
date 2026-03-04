package com.recipe.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private final String url;
    private final String fileName;
    private final long fileSize;
}
