package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.dto.response.AffectedRecipeResponse;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.IngredientService;
import com.recipe.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(IngredientController.class)
@Import(TestSecurityConfig.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngredientService ingredientService;

    @MockitoBean
    private UserService userService;

    private static UsernamePasswordAuthenticationToken purchaserAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "purchaser@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_PURCHASER")));
    }

    private User createPurchaserUser() {
        return User.builder()
                .id(1L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).enabled(true).build();
    }

    private Ingredient createTestIngredient() {
        return Ingredient.builder()
                .id(1L).name("トマト").category("野菜").standardUnit("個")
                .seasonalFlag(true).supplyStatus(SupplyStatus.AVAILABLE).supplier("農家A")
                .build();
    }

    @Test
    void 食材作成_正常系_201が返る() throws Exception {
        User user = createPurchaserUser();
        Ingredient ingredient = createTestIngredient();

        when(userService.getUserById(1L)).thenReturn(user);
        when(ingredientService.createIngredient(any(CreateIngredientRequest.class), eq(user))).thenReturn(ingredient);
        when(ingredientService.getCurrentPrice(1L)).thenReturn(null);
        when(ingredientService.getPriceHistory(1L)).thenReturn(List.of());
        when(ingredientService.getSeasons(1L)).thenReturn(List.of());

        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト").category("野菜").standardUnit("個").build();

        mockMvc.perform(post("/api/ingredients")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("トマト"))
                .andExpect(jsonPath("$.category").value("野菜"));
    }

    @Test
    void 食材作成_異常系_名前空で400() throws Exception {
        User user = createPurchaserUser();
        when(userService.getUserById(1L)).thenReturn(user);

        CreateIngredientRequest request = CreateIngredientRequest.builder().name("").build();

        mockMvc.perform(post("/api/ingredients")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 食材一覧_正常系_ページネーション付きで取得できる() throws Exception {
        Ingredient ingredient = createTestIngredient();
        when(ingredientService.listIngredients(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ingredient)));
        when(ingredientService.getCurrentPrice(1L)).thenReturn(null);

        mockMvc.perform(get("/api/ingredients")
                        .with(authentication(purchaserAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("トマト"));
    }

    @Test
    void 食材詳細_正常系_詳細情報が返る() throws Exception {
        Ingredient ingredient = createTestIngredient();
        when(ingredientService.getIngredientById(1L)).thenReturn(ingredient);
        when(ingredientService.getCurrentPrice(1L)).thenReturn(null);
        when(ingredientService.getPriceHistory(1L)).thenReturn(List.of());
        when(ingredientService.getSeasons(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/ingredients/1")
                        .with(authentication(purchaserAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("トマト"))
                .andExpect(jsonPath("$.supplyStatus").value("AVAILABLE"));
    }

    @Test
    void 価格追加_正常系_201が返る() throws Exception {
        User user = createPurchaserUser();
        IngredientPrice price = IngredientPrice.builder()
                .id(1L).unitPrice(new BigDecimal("150")).pricePerUnit("個")
                .effectiveFrom(LocalDate.of(2026, 3, 1)).build();

        when(userService.getUserById(1L)).thenReturn(user);
        when(ingredientService.addPrice(eq(1L), any(CreateIngredientPriceRequest.class), eq(user)))
                .thenReturn(price);

        CreateIngredientPriceRequest request = CreateIngredientPriceRequest.builder()
                .unitPrice(new BigDecimal("150")).pricePerUnit("個")
                .effectiveFrom(LocalDate.of(2026, 3, 1)).build();

        mockMvc.perform(post("/api/ingredients/1/prices")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitPrice").value(150));
    }

    @Test
    void 影響レシピ_正常系_一覧が返る() throws Exception {
        AffectedRecipeResponse response = AffectedRecipeResponse.builder()
                .recipeId(10L).recipeTitle("トマトパスタ").build();

        when(ingredientService.getAffectedRecipes(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/ingredients/1/affected-recipes")
                        .with(authentication(purchaserAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeId").value(10))
                .andExpect(jsonPath("$[0].recipeTitle").value("トマトパスタ"));
    }
}
