package com.recipe.manager.service;

import com.recipe.manager.dto.response.CrossAnalysisResponse;
import com.recipe.manager.entity.FeedbackSummary;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.User;
import com.recipe.manager.repository.FeedbackSummaryRepository;
import com.recipe.manager.repository.MonthlySalesRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.StoreRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private MonthlySalesRepository monthlySalesRepository;

    @Mock
    private RecipeCostRepository recipeCostRepository;

    @Mock
    private FeedbackSummaryRepository feedbackSummaryRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AnalysisService analysisService;

    private Store testStore;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        testStore = Store.builder().id(1L).storeCode("STORE001").name("テスト店舗").build();
        User creator = User.builder().id(1L).name("Chef").role(Role.CHEF).email("chef@example.com").build();
        testRecipe = Recipe.builder()
                .id(1L).title("テストレシピ").status(RecipeStatus.PUBLISHED)
                .createdBy(creator).ingredients(new ArrayList<>()).cookingSteps(new ArrayList<>())
                .build();
    }

    @Test
    void クロス分析_正常系_高満足度低原価率() {
        MonthlySales sales = MonthlySales.builder()
                .id(1L).store(testStore).recipe(testRecipe)
                .salesMonth("2026-01").quantity(100)
                .salesAmount(new BigDecimal("200000.00"))
                .build();

        RecipeCost recipeCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe)
                .totalIngredientCost(new BigDecimal("500.00"))
                .build();

        FeedbackSummary summary = FeedbackSummary.builder()
                .id(1L).recipe(testRecipe)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .avgSatisfaction(new BigDecimal("4.50"))
                .avgEmotion(new BigDecimal("4.00"))
                .feedbackCount(10)
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(monthlySalesRepository.findByStoreIdAndSalesMonthOrderByRecipeId(1L, "2026-01"))
                .thenReturn(List.of(sales));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(recipeCost));
        when(feedbackSummaryRepository.findByRecipeIdOrderByPeriodStartAsc(1L))
                .thenReturn(List.of(summary));

        List<CrossAnalysisResponse> results = analysisService.getCrossAnalysis(1L, "2026-01");

        assertEquals(1, results.size());
        CrossAnalysisResponse result = results.get(0);
        assertEquals("テストレシピ", result.getRecipeTitle());
        assertEquals(new BigDecimal("4.50"), result.getAvgSatisfaction());
        assertEquals(new BigDecimal("200000.00"), result.getTotalSalesAmount());
        // 原価率: (500 * 100) / 200000 * 100 = 25.00%
        assertEquals(new BigDecimal("25.00"), result.getTheoreticalFoodCostRate());
        assertTrue(result.getInsight().contains("収益性と顧客満足度のバランスが優れています"));
    }

    @Test
    void クロス分析_正常系_高満足度高原価率() {
        MonthlySales sales = MonthlySales.builder()
                .id(1L).store(testStore).recipe(testRecipe)
                .salesMonth("2026-01").quantity(100)
                .salesAmount(new BigDecimal("100000.00"))
                .build();

        RecipeCost recipeCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe)
                .totalIngredientCost(new BigDecimal("500.00"))
                .build();

        FeedbackSummary summary = FeedbackSummary.builder()
                .id(1L).recipe(testRecipe)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .avgSatisfaction(new BigDecimal("4.50"))
                .feedbackCount(10)
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(monthlySalesRepository.findByStoreIdAndSalesMonthOrderByRecipeId(1L, "2026-01"))
                .thenReturn(List.of(sales));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(recipeCost));
        when(feedbackSummaryRepository.findByRecipeIdOrderByPeriodStartAsc(1L))
                .thenReturn(List.of(summary));

        List<CrossAnalysisResponse> results = analysisService.getCrossAnalysis(1L, "2026-01");

        assertEquals(1, results.size());
        // 原価率: (500 * 100) / 100000 * 100 = 50.00% > 35%
        assertTrue(results.get(0).getInsight().contains("感動度は高いが収益性に課題"));
    }

    @Test
    void クロス分析_正常系_フィードバックなし() {
        MonthlySales sales = MonthlySales.builder()
                .id(1L).store(testStore).recipe(testRecipe)
                .salesMonth("2026-01").quantity(50)
                .salesAmount(new BigDecimal("100000.00"))
                .build();

        RecipeCost recipeCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe)
                .totalIngredientCost(new BigDecimal("300.00"))
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(monthlySalesRepository.findByStoreIdAndSalesMonthOrderByRecipeId(1L, "2026-01"))
                .thenReturn(List.of(sales));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(recipeCost));
        when(feedbackSummaryRepository.findByRecipeIdOrderByPeriodStartAsc(1L))
                .thenReturn(List.of());

        List<CrossAnalysisResponse> results = analysisService.getCrossAnalysis(1L, "2026-01");

        assertEquals(1, results.size());
        assertNotNull(results.get(0).getInsight());
    }
}
