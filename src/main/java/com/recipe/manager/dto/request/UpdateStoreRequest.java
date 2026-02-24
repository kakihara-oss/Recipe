package com.recipe.manager.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStoreRequest {

    @Size(max = 200, message = "店舗名は200文字以内で入力してください")
    private String name;

    @Size(max = 500, message = "所在地は500文字以内で入力してください")
    private String location;
}
