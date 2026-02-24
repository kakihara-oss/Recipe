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
import java.util.ArrayList;
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

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private static UsernamePasswordAuthenticationToken chefAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "chef@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_CHEF")));
    }

    private User createChefUser() {
        return User.builder()
                .id(1L).email("chef@example.com").name("Chef").role(Role.CHEF)
                .enabled(true).createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();
    }

    private RecipeCost createTestRecipeCost() {
        User creator = User.builder().id(1L).name("Chef").role(Role.CHEF).email("chef@example.com").build();
        Recipe recipe = Recipe.builder()
                .id(1L).title("テストレシピ").status(RecipeStatus.PUBLISHED)
                .createdBy(creator).ingredients(new ArrayList<>()).cookingSteps(new ArrayList<>())
                .createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();

        return RecipeCost.builder()
                .id(1L)
                .recipe(recipe)
                .totalIngredientCost(new BigDecimal("650.00"))
                .targetGrossMarginRate(new BigDecimal("0.7000"))
                .recommendedPrice(new BigDecimal("2166.67"))
                .currentPrice(new BigDecimal("2500.00"))
                .lastCalculatedAt(FIXED_TIME)
                .createdAt(FIXED_TIME)
                .updatedAt(FIXED_TIME)
                .build();
    }

    @Test
    void 原価取得_正常系_200が返る() throws Exception {
        User user = createChefUser();
        RecipeCost cost = createTestRecipeCost();

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeCostService.getRecipeCost(1L, user)).thenReturn(cost);

        mockMvc.perform(get("/api/recipes/1/cost")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.totalIngredientCost").value(650.00))
                .andExpect(jsonPath("$.recommendedPrice").value(2166.67));
    }

    @Test
    void 原価計算_正常系_200が返る() throws Exception {
        User user = createChefUser();
        RecipeCost cost = createTestRecipeCost();

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeCostService.calculateAndSave(1L, user)).thenReturn(cost);

        mockMvc.perform(post("/api/recipes/1/cost/calculate")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngredientCost").value(650.00));
    }

    @Test
    void 原価更新_正常系_200が返る() throws Exception {
        User user = createChefUser();
        RecipeCost cost = createTestRecipeCost();

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeCostService.updateRecipeCost(eq(1L), any(UpdateRecipeCostRequest.class), eq(user)))
                .thenReturn(cost);

        UpdateRecipeCostRequest request = UpdateRecipeCostRequest.builder()
                .currentPrice(new BigDecimal("2500.00"))
                .build();

        mockMvc.perform(put("/api/recipes/1/cost")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPrice").value(2500.00));
    }

    @Test
    void 認証なし_401が返る() throws Exception {
        mockMvc.perform(get("/api/recipes/1/cost"))
                .andExpect(status().isUnauthorized());
    }
}
