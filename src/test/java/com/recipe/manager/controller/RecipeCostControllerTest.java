package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.UpdateRecipeCostRequest;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.RecipeCostService;
import com.recipe.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeCostController.class)
@Import(TestSecurityConfig.class)
class RecipeCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeCostService recipeCostService;

    @MockitoBean
    private UserService userService;

    private static UsernamePasswordAuthenticationToken chefAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "chef@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_CHEF")));
    }

    private User createChefUser() {
        return User.builder()
                .id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).enabled(true).build();
    }

    private RecipeCost createTestCost() {
        User user = createChefUser();
        Recipe recipe = Recipe.builder()
                .id(1L).title("トマトパスタ").status(RecipeStatus.PUBLISHED).createdBy(user).build();
        return RecipeCost.builder()
                .id(1L).recipe(recipe)
                .totalIngredientCost(new BigDecimal("800"))
                .targetMarginRate(new BigDecimal("70"))
                .recommendedPrice(new BigDecimal("2667"))
                .currentPrice(new BigDecimal("2500"))
                .lastCalculatedAt(LocalDateTime.of(2026, 3, 1, 12, 0))
                .build();
    }

    @Test
    void 原価取得_正常系_原価情報が返る() throws Exception {
        RecipeCost cost = createTestCost();
        when(recipeCostService.getRecipeCost(1L)).thenReturn(cost);

        mockMvc.perform(get("/api/recipes/1/cost")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.totalIngredientCost").value(800))
                .andExpect(jsonPath("$.targetMarginRate").value(70));
    }

    @Test
    void 原価計算_正常系_計算結果が返る() throws Exception {
        RecipeCost cost = createTestCost();
        when(recipeCostService.calculateCost(1L)).thenReturn(cost);

        mockMvc.perform(post("/api/recipes/1/cost/calculate")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngredientCost").value(800));
    }

    @Test
    void 原価設定更新_正常系_粗利率と売価を更新できる() throws Exception {
        User user = createChefUser();
        RecipeCost cost = createTestCost();

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeCostService.updateCostSettings(eq(1L), any(UpdateRecipeCostRequest.class), eq(user)))
                .thenReturn(cost);

        UpdateRecipeCostRequest request = UpdateRecipeCostRequest.builder()
                .targetMarginRate(new BigDecimal("70"))
                .currentPrice(new BigDecimal("2500"))
                .build();

        mockMvc.perform(put("/api/recipes/1/cost")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetMarginRate").value(70));
    }

    @Test
    void 警告レシピ_正常系_一覧が返る() throws Exception {
        RecipeCost cost = createTestCost();
        when(recipeCostService.getWarningRecipes()).thenReturn(List.of(cost));

        mockMvc.perform(get("/api/recipes/costs/warnings")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeId").value(1));
    }
}
