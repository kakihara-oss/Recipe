package com.recipe.manager.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIngredientPriceRequest {

    @NotNull(message = "単価は必須です")
    @Positive(message = "単価は正の数で入力してください")
    private BigDecimal unitPrice;

    @Size(max = 50, message = "単価単位は50文字以内で入力してください")
    private String pricePerUnit;

    @NotNull(message = "有効開始日は必須です")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
