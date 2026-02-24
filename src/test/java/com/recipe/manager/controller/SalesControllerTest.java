package com.recipe.manager.controller;

import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.response.CrossAnalysisResponse;
import com.recipe.manager.dto.response.CsvUploadResponse;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.AnalysisService;
import com.recipe.manager.service.SalesService;
import com.recipe.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesController.class)
@Import(TestSecurityConfig.class)
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesService salesService;

    @MockitoBean
    private AnalysisService analysisService;

    @MockitoBean
    private UserService userService;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private static UsernamePasswordAuthenticationToken serviceAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "service@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
    }

    private User createServiceUser() {
        return User.builder()
                .id(1L).email("service@example.com").name("Service").role(Role.SERVICE)
                .enabled(true).createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();
    }

    @Test
    void CSVアップロード_正常系_200が返る() throws Exception {
        User user = createServiceUser();
        when(userService.getUserById(1L)).thenReturn(user);

        CsvUploadResponse response = CsvUploadResponse.builder()
                .totalRows(2).successRows(2).errorRows(0).errors(List.of()).build();
        when(salesService.uploadPosCsv(any(), eq(user))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                "STORE001,1,2026-01,100,150000\n".getBytes());

        mockMvc.perform(multipart("/api/sales/upload")
                        .file(file)
                        .with(authentication(serviceAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.successRows").value(2));
    }

    @Test
    void 売上取得_正常系_200が返る() throws Exception {
        Store store = Store.builder().id(1L).storeCode("STORE001").name("テスト店舗").build();
        User creator = User.builder().id(1L).name("Chef").role(Role.CHEF).email("chef@example.com").build();
        Recipe recipe = Recipe.builder()
                .id(1L).title("テストレシピ").status(RecipeStatus.PUBLISHED)
                .createdBy(creator).ingredients(new ArrayList<>()).cookingSteps(new ArrayList<>())
                .build();
        MonthlySales sales = MonthlySales.builder()
                .id(1L).store(store).recipe(recipe)
                .salesMonth("2026-01").quantity(100)
                .salesAmount(new BigDecimal("150000.00"))
                .build();

        when(salesService.getSalesByStoreAndMonth(1L, "2026-01")).thenReturn(List.of(sales));

        mockMvc.perform(get("/api/sales/stores/1/monthly/2026-01")
                        .with(authentication(serviceAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salesMonth").value("2026-01"))
                .andExpect(jsonPath("$[0].quantity").value(100));
    }

    @Test
    void 店舗間比較_正常系_200が返る() throws Exception {
        Store store = Store.builder().id(1L).storeCode("STORE001").name("テスト店舗").build();
        StoreMonthlyFoodCost cost = StoreMonthlyFoodCost.builder()
                .id(1L).store(store).salesMonth("2026-01")
                .theoreticalFoodCost(new BigDecimal("50000.00"))
                .totalSales(new BigDecimal("150000.00"))
                .theoreticalFoodCostRate(new BigDecimal("33.33"))
                .calculatedAt(FIXED_TIME)
                .build();

        when(salesService.getStoreComparison("2026-01")).thenReturn(List.of(cost));

        mockMvc.perform(get("/api/sales/food-cost/comparison/2026-01")
                        .with(authentication(serviceAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("テスト店舗"))
                .andExpect(jsonPath("$[0].theoreticalFoodCostRate").value(33.33));
    }

    @Test
    void クロス分析_正常系_200が返る() throws Exception {
        CrossAnalysisResponse analysis = CrossAnalysisResponse.builder()
                .recipeId(1L).recipeTitle("テストレシピ")
                .avgSatisfaction(new BigDecimal("4.50"))
                .totalSalesAmount(new BigDecimal("200000.00"))
                .totalQuantity(100)
                .totalIngredientCost(new BigDecimal("50000.00"))
                .theoreticalFoodCostRate(new BigDecimal("25.00"))
                .insight("高満足度・低原価率：収益性と顧客満足度のバランスが優れています")
                .build();

        when(analysisService.getCrossAnalysis(1L, "2026-01")).thenReturn(List.of(analysis));

        mockMvc.perform(get("/api/sales/analysis/cross/1/2026-01")
                        .with(authentication(serviceAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeTitle").value("テストレシピ"))
                .andExpect(jsonPath("$[0].insight").exists());
    }

    @Test
    void 認証なし_401が返る() throws Exception {
        mockMvc.perform(get("/api/sales/stores/1/monthly/2026-01"))
                .andExpect(status().isUnauthorized());
    }
}
