package com.recipe.manager.dto.request;

import com.recipe.manager.entity.SupplyStatus;
import jakarta.validation.constraints.NotBlank;
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
public class CreateIngredientRequest {

    @NotBlank(message = "食材名は必須です")
    @Size(max = 255, message = "食材名は255文字以内で入力してください")
    private String name;

    @Size(max = 100, message = "カテゴリは100文字以内で入力してください")
    private String category;

    @Size(max = 50, message = "標準単位は50文字以内で入力してください")
    private String standardUnit;

    private SupplyStatus supplyStatus;

    @Size(max = 255, message = "仕入先は255文字以内で入力してください")
    private String supplier;
}
