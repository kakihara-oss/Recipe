package com.recipe.manager.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRecipeCostRequest {

    @DecimalMin(value = "0.0", message = "目標粗利率は0以上で入力してください")
    @DecimalMax(value = "1.0", message = "目標粗利率は1以下で入力してください")
    private BigDecimal targetGrossMarginRate;

    @Positive(message = "現在売価は正の数で入力してください")
    private BigDecimal currentPrice;
}
