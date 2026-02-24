package com.recipe.manager.service;

import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.repository.MonthlySalesRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.repository.StoreMonthlyFoodCostRepository;
import com.recipe.manager.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
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
class SalesServiceTest {

    @Mock
    private MonthlySalesRepository monthlySalesRepository;

    @Mock
    private StoreMonthlyFoodCostRepository storeMonthlyFoodCostRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeCostRepository recipeCostRepository;

    @InjectMocks
    private SalesService salesService;

    private User serviceUser;
    private User chefUser;
    private Store testStore;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        serviceUser = User.builder().id(1L).email("service@example.com").name("Service").role(Role.SERVICE).build();
        chefUser = User.builder().id(2L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        testStore = Store.builder().id(1L).storeCode("STORE001").name("テスト店舗").build();

        User creator = User.builder().id(1L).name("Chef").role(Role.CHEF).email("chef@example.com").build();
        testRecipe = Recipe.builder()
                .id(1L).title("テストレシピ").status(RecipeStatus.PUBLISHED)
                .createdBy(creator).ingredients(new ArrayList<>()).cookingSteps(new ArrayList<>())
                .build();
    }

    @Test
    void CSVアップロード_正常系_正しいCSVを取り込める() {
        String csvContent = "STORE001,1,2026-01,100,150000\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes());

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(testStore));
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(testRecipe));
        when(monthlySalesRepository.save(any(MonthlySales.class))).thenAnswer(invocation -> {
            MonthlySales saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var result = salesService.uploadPosCsv(file, serviceUser);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessRows());
        assertEquals(0, result.getErrorRows());
    }

    @Test
    void CSVアップロード_異常系_存在しない店舗コード() {
        String csvContent = "INVALID,1,2026-01,100,150000\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes());

        when(storeRepository.findByStoreCode("INVALID")).thenReturn(Optional.empty());

        var result = salesService.uploadPosCsv(file, serviceUser);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessRows());
        assertEquals(1, result.getErrorRows());
    }

    @Test
    void CSVアップロード_異常系_不正な年月フォーマット() {
        String csvContent = "STORE001,1,202601,100,150000\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes());

        var result = salesService.uploadPosCsv(file, serviceUser);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessRows());
        assertEquals(1, result.getErrorRows());
    }

    @Test
    void CSVアップロード_異常系_シェフはアップロードできない() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                "STORE001,1,2026-01,100,150000\n".getBytes());

        assertThrows(ForbiddenException.class, () ->
                salesService.uploadPosCsv(file, chefUser));
    }

    @Test
    void CSVアップロード_正常系_ヘッダー行をスキップする() {
        String csvContent = "店舗コード,レシピコード,対象年月,出数,売上金額\nSTORE001,1,2026-01,100,150000\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes());

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(testStore));
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(testRecipe));
        when(monthlySalesRepository.save(any(MonthlySales.class))).thenAnswer(invocation -> {
            MonthlySales saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var result = salesService.uploadPosCsv(file, serviceUser);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessRows());
    }

    @Test
    void 理論原価計算_正常系_正しく計算できる() {
        MonthlySales sales = MonthlySales.builder()
                .id(1L).store(testStore).recipe(testRecipe)
                .salesMonth("2026-01").quantity(100)
                .salesAmount(new BigDecimal("150000.00"))
                .build();

        RecipeCost recipeCost = RecipeCost.builder()
                .id(1L).recipe(testRecipe)
                .totalIngredientCost(new BigDecimal("500.00"))
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(monthlySalesRepository.findByStoreIdAndSalesMonth(1L, "2026-01"))
                .thenReturn(List.of(sales));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(recipeCost));
        when(storeMonthlyFoodCostRepository.findByStoreIdAndSalesMonth(1L, "2026-01"))
                .thenReturn(Optional.empty());
        when(storeMonthlyFoodCostRepository.save(any(StoreMonthlyFoodCost.class)))
                .thenAnswer(invocation -> {
                    StoreMonthlyFoodCost saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        StoreMonthlyFoodCost result = salesService.calculateTheoreticalFoodCost(1L, "2026-01", serviceUser);

        assertNotNull(result);
        // 理論原価: 500 * 100 = 50000
        assertEquals(new BigDecimal("50000.00"), result.getTheoreticalFoodCost());
        assertEquals(new BigDecimal("150000.00"), result.getTotalSales());
        // 理論原価率: 50000 / 150000 * 100 = 33.33%
        assertEquals(new BigDecimal("33.33"), result.getTheoreticalFoodCostRate());
    }

    @Test
    void 理論原価計算_異常系_売上データなし() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(monthlySalesRepository.findByStoreIdAndSalesMonth(1L, "2026-01"))
                .thenReturn(List.of());

        assertThrows(BusinessLogicException.class, () ->
                salesService.calculateTheoreticalFoodCost(1L, "2026-01", serviceUser));
    }
}
