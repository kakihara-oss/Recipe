package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStoreRequest {

    @NotBlank
    @Size(max = 50)
    private String storeCode;

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String location;
}
