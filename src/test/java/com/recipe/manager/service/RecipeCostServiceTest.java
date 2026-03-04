package com.recipe.manager.service;

import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeIngredientRepository;
import com.recipe.manager.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeCostServiceTest {

    @Mock
    private RecipeCostRepository recipeCostRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientPriceRepository ingredientPriceRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @InjectMocks
    private RecipeCostService recipeCostService;

    private User chefUser;
    private User purchaserUser;
    private User producerUser;
    private User serviceUser;
    private Recipe testRecipe;
    private Ingredient ingredient1;
    private Ingredient ingredient2;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        purchaserUser = User.builder().id(2L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        producerUser = User.builder().id(3L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        serviceUser = User.builder().id(4L).email("service@example.com").name("Service").role(Role.SERVICE).build();

        ingredient1 = Ingredient.builder().id(1L).name("トマト").build();
        ingredient2 = Ingredient.builder().id(2L).name("バジル").build();

        testRecipe = Recipe.builder()
                .id(1L).title("トマトパスタ").status(RecipeStatus.PUBLISHED).createdBy(chefUser)
                .ingredients(new ArrayList<>())
                .build();

        RecipeIngredient ri1 = RecipeIngredient.builder()
                .id(1L).recipe(testRecipe).ingredient(ingredient1)
                .quantity(new BigDecimal("3")).unit("個").build();
        RecipeIngredient ri2 = RecipeIngredient.builder()
                .id(2L).recipe(testRecipe).ingredient(ingredient2)
                .quantity(new BigDecimal("10")).unit("枚").build();

        testRecipe.getIngredients().add(ri1);
        testRecipe.getIngredients().add(ri2);
    }

    @Test
    void 原価計算_正常系_食材価格から原価を計算できる() {
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(testRecipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice price1 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("100")).build();
        IngredientPrice price2 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("50")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(price1));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(price2));
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(inv -> {
            RecipeCost rc = inv.getArgument(0);
            rc.setId(1L);
            return rc;
        });

        RecipeCost result = recipeCostService.calculateCost(1L);

        // 3 * 100 + 10 * 50 = 800
        assertEquals(0, new BigDecimal("800.00").compareTo(result.getTotalIngredientCost()));
        assertNotNull(result.getLastCalculatedAt());
    }

    @Test
    void 原価計算_正常系_価格未登録の食材はスキップする() {
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(testRecipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice price1 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("100")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(price1));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(inv -> {
            RecipeCost rc = inv.getArgument(0);
            rc.setId(1L);
            return rc;
        });

        RecipeCost result = recipeCostService.calculateCost(1L);

        // 3 * 100 = 300 (バジルは価格未登録なのでスキップ)
        assertEquals(0, new BigDecimal("300.00").compareTo(result.getTotalIngredientCost()));
    }

    @Test
    void 原価計算_正常系_推奨売価を計算できる() {
        RecipeCost existingCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe).targetMarginRate(new BigDecimal("70")).build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(testRecipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(existingCost));

        IngredientPrice price1 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("100")).build();
        IngredientPrice price2 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("50")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(price1));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(price2));
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(inv -> inv.getArgument(0));

        RecipeCost result = recipeCostService.calculateCost(1L);

        // 原価800, 粗利率70% -> 推奨売価 = 800 / (1 - 0.70) = 800 / 0.30 = 2667 (切り上げ)
        assertNotNull(result.getRecommendedPrice());
        assertEquals(0, new BigDecimal("2667").compareTo(result.getRecommendedPrice()));
    }

    @Test
    void 原価取得_異常系_存在しないレシピ() {
        when(recipeCostRepository.findByRecipeId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recipeCostService.getRecipeCost(999L));
    }

    @Test
    void 原価設定更新_正常系_CHEFが更新できる() {
        RecipeCost existingCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe)
                .totalIngredientCost(new BigDecimal("800"))
                .build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(testRecipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(existingCost));
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRecipeCostRequest request = UpdateRecipeCostRequest.builder()
                .targetMarginRate(new BigDecimal("65"))
                .currentPrice(new BigDecimal("2500"))
                .build();

        RecipeCost result = recipeCostService.updateCostSettings(1L, request, chefUser);

        assertEquals(0, new BigDecimal("65").compareTo(result.getTargetMarginRate()));
        assertEquals(0, new BigDecimal("2500").compareTo(result.getCurrentPrice()));
    }

    @Test
    void 原価設定更新_異常系_SERVICEは更新できない() {
        UpdateRecipeCostRequest request = UpdateRecipeCostRequest.builder()
                .targetMarginRate(new BigDecimal("65")).build();

        assertThrows(ForbiddenException.class,
                () -> recipeCostService.updateCostSettings(1L, request, serviceUser));
    }

    @Test
    void 影響レシピ再計算_正常系_食材変更時に対象レシピを再計算する() {
        RecipeIngredient ri = RecipeIngredient.builder()
                .id(1L).recipe(testRecipe).ingredient(ingredient1).quantity(new BigDecimal("3")).build();

        when(recipeIngredientRepository.findByIngredientId(1L)).thenReturn(List.of(ri));
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(testRecipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice price1 = IngredientPrice.builder()
                .unitPrice(new BigDecimal("120")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(price1));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(inv -> {
            RecipeCost rc = inv.getArgument(0);
            rc.setId(1L);
            return rc;
        });

        List<RecipeCost> results = recipeCostService.recalculateAffectedRecipes(1L);

        assertEquals(1, results.size());
    }
}
