package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.IngredientService;
import com.recipe.manager.service.RecipeCostService;
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
    private RecipeCostService recipeCostService;

    @MockitoBean
    private UserService userService;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private static UsernamePasswordAuthenticationToken purchaserAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "purchaser@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_PURCHASER")));
    }

    private User createPurchaserUser() {
        return User.builder()
                .id(1L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER)
                .enabled(true).createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();
    }

    private Ingredient createTestIngredient() {
        return Ingredient.builder()
                .id(1L)
                .name("トマト")
                .category("野菜")
                .standardUnit("kg")
                .supplyStatus(SupplyStatus.AVAILABLE)
                .supplier("テスト仕入先")
                .createdAt(FIXED_TIME)
                .updatedAt(FIXED_TIME)
                .build();
    }

    @Test
    void 食材作成_正常系_201が返る() throws Exception {
        User user = createPurchaserUser();
        Ingredient ingredient = createTestIngredient();

        when(userService.getUserById(1L)).thenReturn(user);
        when(ingredientService.createIngredient(any(CreateIngredientRequest.class), eq(user)))
                .thenReturn(ingredient);

        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト")
                .category("野菜")
                .standardUnit("kg")
                .supplyStatus(SupplyStatus.AVAILABLE)
                .supplier("テスト仕入先")
                .build();

        mockMvc.perform(post("/api/ingredients")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("トマト"))
                .andExpect(jsonPath("$.category").value("野菜"))
                .andExpect(jsonPath("$.supplyStatus").value("AVAILABLE"));
    }

    @Test
    void 食材取得_正常系_200が返る() throws Exception {
        Ingredient ingredient = createTestIngredient();
        when(ingredientService.getIngredientById(1L)).thenReturn(ingredient);

        mockMvc.perform(get("/api/ingredients/1")
                        .with(authentication(purchaserAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("トマト"));
    }

    @Test
    void 食材一覧_正常系_200が返る() throws Exception {
        Ingredient ingredient = createTestIngredient();
        when(ingredientService.listIngredients(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ingredient)));

        mockMvc.perform(get("/api/ingredients")
                        .with(authentication(purchaserAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("トマト"));
    }

    @Test
    void 食材更新_正常系_200が返る() throws Exception {
        User user = createPurchaserUser();
        Ingredient updated = createTestIngredient();
        updated.setName("ミニトマト");

        when(userService.getUserById(1L)).thenReturn(user);
        when(ingredientService.updateIngredient(eq(1L), any(UpdateIngredientRequest.class), eq(user)))
                .thenReturn(updated);

        UpdateIngredientRequest request = UpdateIngredientRequest.builder()
                .name("ミニトマト")
                .build();

        mockMvc.perform(put("/api/ingredients/1")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ミニトマト"));
    }

    @Test
    void 価格追加_正常系_201が返る() throws Exception {
        User user = createPurchaserUser();
        IngredientPrice price = IngredientPrice.builder()
                .id(1L)
                .unitPrice(new BigDecimal("300.00"))
                .pricePerUnit("1kg")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(userService.getUserById(1L)).thenReturn(user);
        when(ingredientService.addPrice(eq(1L), any(CreateIngredientPriceRequest.class), eq(user)))
                .thenReturn(price);

        CreateIngredientPriceRequest request = CreateIngredientPriceRequest.builder()
                .unitPrice(new BigDecimal("300.00"))
                .pricePerUnit("1kg")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/ingredients/1/prices")
                        .with(authentication(purchaserAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitPrice").value(300.00))
                .andExpect(jsonPath("$.pricePerUnit").value("1kg"));
    }

    @Test
    void 認証なし_401が返る() throws Exception {
        mockMvc.perform(get("/api/ingredients"))
                .andExpect(status().isUnauthorized());
    }
}
