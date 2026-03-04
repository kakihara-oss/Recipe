package com.recipe.manager.service;

import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.MonthlySalesRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.repository.StoreMonthlyFoodCostRepository;
import com.recipe.manager.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    private User createUser(Role role) {
        return User.builder().id(1L).email("test@example.com").role(role).build();
    }

    @Test
    void CSVアップロード_正常系_データが取り込まれる() throws Exception {
        String csv = "STORE001,1,2026-01,10,50000\nSTORE001,2,2026-01,5,30000\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        Store store = Store.builder().id(1L).storeCode("STORE001").name("本店").build();
        Recipe recipe1 = Recipe.builder().id(1L).title("レシピA").build();
        Recipe recipe2 = Recipe.builder().id(2L).title("レシピB").build();

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(recipeRepository.findByIdAndStatusNot(anyLong(), any())).thenReturn(Optional.of(recipe1), Optional.of(recipe2));
        when(monthlySalesRepository.findByStoreIdAndRecipeIdAndTargetMonth(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(monthlySalesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = salesService.uploadPosCsv(file, createUser(Role.PRODUCER));
        assertThat(result.getImportedCount()).isEqualTo(2);
        assertThat(result.getSkippedCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void CSVアップロード_異常系_CHEF権限ではアップロードできない() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        assertThatThrownBy(() -> salesService.uploadPosCsv(file, createUser(Role.CHEF)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void CSVアップロード_異常系_不正な店舗コードはスキップされる() throws Exception {
        String csv = "UNKNOWN,1,2026-01,10,50000\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        when(storeRepository.findByStoreCode("UNKNOWN")).thenReturn(Optional.empty());

        var result = salesService.uploadPosCsv(file, createUser(Role.PRODUCER));
        assertThat(result.getImportedCount()).isEqualTo(0);
        assertThat(result.getSkippedCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void 理論原価計算_正常系_原価率が計算される() {
        Store store = Store.builder().id(1L).storeCode("S1").name("本店").build();
        Recipe recipe = Recipe.builder().id(1L).title("レシピA").build();

        MonthlySales sales = MonthlySales.builder()
                .store(store).recipe(recipe).targetMonth("2026-01")
                .quantity(10).salesAmount(new BigDecimal("100000")).build();

        RecipeCost recipeCost = RecipeCost.builder()
                .recipe(recipe).totalIngredientCost(new BigDecimal("3000")).build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(monthlySalesRepository.findByStoreIdAndTargetMonth(1L, "2026-01"))
                .thenReturn(List.of(sales));
        when(recipeCostRepository.findByRecipeId(1L)).thenReturn(Optional.of(recipeCost));
        when(storeMonthlyFoodCostRepository.findByStoreIdAndTargetMonth(1L, "2026-01"))
                .thenReturn(Optional.empty());
        when(storeMonthlyFoodCostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreMonthlyFoodCost result = salesService.calculateMonthlyFoodCost(1L, "2026-01");
        // 3000 * 10 = 30000 theoretical cost, 100000 sales, rate = 30.00%
        assertThat(result.getTheoreticalFoodCost()).isEqualByComparingTo("30000");
        assertThat(result.getTotalSales()).isEqualByComparingTo("100000");
        assertThat(result.getTheoreticalFoodCostRate()).isEqualByComparingTo("30.00");
    }

    @Test
    void 理論原価計算_異常系_売上データなしはエラー() {
        Store store = Store.builder().id(1L).storeCode("S1").name("本店").build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(monthlySalesRepository.findByStoreIdAndTargetMonth(1L, "2026-01"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> salesService.calculateMonthlyFoodCost(1L, "2026-01"))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    void 理論原価計算_異常系_存在しない店舗はエラー() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salesService.calculateMonthlyFoodCost(99L, "2026-01"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
