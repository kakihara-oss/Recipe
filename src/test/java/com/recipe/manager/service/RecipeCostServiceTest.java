package com.recipe.manager.service;

import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.RecipeCostRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeCostServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeCostRepository recipeCostRepository;

    @Mock
    private IngredientPriceRepository ingredientPriceRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeCostService recipeCostService;

    private User chefUser;
    private User purchaserUser;
    private User serviceUser;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        purchaserUser = User.builder().id(2L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        serviceUser = User.builder().id(3L).email("service@example.com").name("Service").role(Role.SERVICE).build();
    }

    private Recipe createTestRecipeWithIngredients() {
        User creator = User.builder().id(1L).name("Chef").role(Role.CHEF).email("chef@example.com").build();

        Ingredient tomato = Ingredient.builder()
                .id(1L).name("トマト").supplyStatus(SupplyStatus.AVAILABLE).build();
        Ingredient basil = Ingredient.builder()
                .id(2L).name("バジル").supplyStatus(SupplyStatus.AVAILABLE).build();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("テストレシピ")
                .status(RecipeStatus.PUBLISHED)
                .createdBy(creator)
                .ingredients(new ArrayList<>())
                .cookingSteps(new ArrayList<>())
                .build();

        RecipeIngredient ri1 = RecipeIngredient.builder()
                .id(1L).recipe(recipe).ingredient(tomato)
                .quantity(new BigDecimal("2.00")).unit("kg").sortOrder(0).build();
        RecipeIngredient ri2 = RecipeIngredient.builder()
                .id(2L).recipe(recipe).ingredient(basil)
                .quantity(new BigDecimal("0.10")).unit("kg").sortOrder(1).build();

        recipe.getIngredients().add(ri1);
        recipe.getIngredients().add(ri2);

        return recipe;
    }

    @Test
    void 原価取得_正常系_シェフが取得できる() {
        Recipe recipe = createTestRecipeWithIngredients();
        RecipeCost cost = RecipeCost.builder()
                .id(1L).recipe(recipe)
                .totalIngredientCost(new BigDecimal("650.00"))
                .targetGrossMarginRate(new BigDecimal("0.7000"))
                .recommendedPrice(new BigDecimal("2166.67"))
                .build();

        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(cost));

        RecipeCost result = recipeCostService.getRecipeCost(1L, chefUser);

        assertNotNull(result);
        assertEquals(new BigDecimal("650.00"), result.getTotalIngredientCost());
    }

    @Test
    void 原価取得_異常系_サービスは取得できない() {
        assertThrows(ForbiddenException.class, () ->
                recipeCostService.getRecipeCost(1L, serviceUser));
    }

    @Test
    void 原価取得_異常系_存在しないレシピ() {
        when(recipeCostRepository.findByRecipeId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recipeCostService.getRecipeCost(999L, chefUser));
    }

    @Test
    void 原価計算_正常系_食材価格から原価を計算できる() {
        Recipe recipe = createTestRecipeWithIngredients();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice tomatoPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("300.00")).build();
        IngredientPrice basilPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("500.00")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(tomatoPrice));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(basilPrice));

        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(invocation -> {
            RecipeCost saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RecipeCost result = recipeCostService.calculateAndSave(1L, chefUser);

        assertNotNull(result);
        // トマト: 300 * 2 = 600, バジル: 500 * 0.10 = 50, 合計 = 650
        assertEquals(new BigDecimal("650.00"), result.getTotalIngredientCost());
        // 推奨売価: 650 / (1 - 0.7) = 2166.67
        assertEquals(new BigDecimal("2166.67"), result.getRecommendedPrice());
    }

    @Test
    void 原価計算_正常系_価格未設定の食材はスキップされる() {
        Recipe recipe = createTestRecipeWithIngredients();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice tomatoPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("300.00")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(tomatoPrice));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(invocation -> {
            RecipeCost saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RecipeCost result = recipeCostService.calculateAndSave(1L, chefUser);

        // トマトのみ: 300 * 2 = 600
        assertEquals(new BigDecimal("600.00"), result.getTotalIngredientCost());
    }

    @Test
    void 原価更新_正常系_売価と目標粗利率を更新できる() {
        Recipe recipe = createTestRecipeWithIngredients();
        RecipeCost existingCost = RecipeCost.builder()
                .id(1L).recipe(recipe)
                .totalIngredientCost(new BigDecimal("650.00"))
                .targetGrossMarginRate(new BigDecimal("0.7000"))
                .build();

        UpdateRecipeCostRequest request = UpdateRecipeCostRequest.builder()
                .targetGrossMarginRate(new BigDecimal("0.6500"))
                .currentPrice(new BigDecimal("2000.00"))
                .build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(existingCost));

        IngredientPrice tomatoPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("300.00")).build();
        IngredientPrice basilPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("500.00")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(tomatoPrice));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(basilPrice));
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeCost result = recipeCostService.updateRecipeCost(1L, request, purchaserUser);

        assertEquals(new BigDecimal("0.6500"), result.getTargetGrossMarginRate());
        assertEquals(new BigDecimal("2000.00"), result.getCurrentPrice());
    }

    @Test
    void 影響レシピ取得_正常系_食材変更による影響レシピを取得できる() {
        Recipe recipe = createTestRecipeWithIngredients();
        RecipeCost existingCost = RecipeCost.builder()
                .id(1L).recipe(recipe)
                .totalIngredientCost(new BigDecimal("600.00"))
                .targetGrossMarginRate(new BigDecimal("0.7000"))
                .currentPrice(new BigDecimal("1800.00"))
                .build();

        when(ingredientRepository.findRecipeIdsByIngredientId(1L)).thenReturn(List.of(1L));
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(existingCost));

        // 価格が上がった場合
        IngredientPrice newTomatoPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("500.00")).build();
        IngredientPrice basilPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("500.00")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(newTomatoPrice));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(basilPrice));

        List<AffectedRecipeResponse> results = recipeCostService.getAffectedRecipes(1L);

        assertEquals(1, results.size());
        AffectedRecipeResponse affected = results.get(0);
        assertEquals(1L, affected.getRecipeId());
        assertEquals("テストレシピ", affected.getRecipeTitle());
        assertEquals(new BigDecimal("600.00"), affected.getPreviousCost());
        // 新原価: 500*2 + 500*0.1 = 1050
        assertEquals(new BigDecimal("1050.00"), affected.getNewCost());
        // 実際粗利率: (1800 - 1050) / 1800 = 0.4167
        assertTrue(affected.isBelowTarget());
    }

    @Test
    void 一括再計算_正常系_食材に関連するレシピの原価を再計算できる() {
        Recipe recipe = createTestRecipeWithIngredients();

        when(ingredientRepository.findRecipeIdsByIngredientId(1L)).thenReturn(List.of(1L));
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        IngredientPrice tomatoPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("300.00")).build();
        IngredientPrice basilPrice = IngredientPrice.builder()
                .unitPrice(new BigDecimal("500.00")).build();

        when(ingredientPriceRepository.findCurrentPrice(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(tomatoPrice));
        when(ingredientPriceRepository.findCurrentPrice(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.of(basilPrice));
        when(recipeCostRepository.save(any(RecipeCost.class))).thenAnswer(invocation -> {
            RecipeCost saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        List<RecipeCost> results = recipeCostService.recalculateByIngredient(1L, chefUser);

        assertEquals(1, results.size());
        assertEquals(new BigDecimal("650.00"), results.get(0).getTotalIngredientCost());
    }
}
