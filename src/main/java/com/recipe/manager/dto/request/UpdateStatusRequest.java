package com.recipe.manager.dto.request;

import com.recipe.manager.entity.RecipeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "ステータスは必須です")
    private RecipeStatus status;
}
