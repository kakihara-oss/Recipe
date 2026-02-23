package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.CreateRecipeRequest;
import com.recipe.manager.dto.request.UpdateRecipeRequest;
import com.recipe.manager.dto.request.UpdateServiceDesignRequest;
import com.recipe.manager.dto.request.UpdateStatusRequest;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeHistory;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.RecipeService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@Import(TestSecurityConfig.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

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

    private Recipe createTestRecipe(User user) {
        return Recipe.builder()
                .id(1L)
                .title("テストレシピ")
                .description("テスト説明")
                .category("メイン")
                .servings(4)
                .status(RecipeStatus.DRAFT)
                .createdBy(user)
                .createdAt(FIXED_TIME)
                .updatedAt(FIXED_TIME)
                .build();
    }

    @Test
    void レシピ作成_正常系_201が返る() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeService.createRecipe(any(CreateRecipeRequest.class), eq(user))).thenReturn(recipe);

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .title("テストレシピ")
                .description("テスト説明")
                .category("メイン")
                .servings(4)
                .build();

        mockMvc.perform(post("/api/recipes")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("テストレシピ"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void レシピ作成_異常系_タイトル空で400() throws Exception {
        User user = createChefUser();
        when(userService.getUserById(1L)).thenReturn(user);

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .title("")
                .build();

        mockMvc.perform(post("/api/recipes")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void レシピ取得_正常系_詳細が返る() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);

        when(recipeService.getRecipeById(1L)).thenReturn(recipe);

        mockMvc.perform(get("/api/recipes/1")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("テストレシピ"))
                .andExpect(jsonPath("$.createdBy.name").value("Chef"));
    }

    @Test
    void レシピ取得_異常系_未認証で401() throws Exception {
        mockMvc.perform(get("/api/recipes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void レシピ一覧_正常系_ページネーション() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);

        when(recipeService.listRecipes(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        mockMvc.perform(get("/api/recipes")
                        .with(authentication(chefAuth()))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("テストレシピ"));
    }

    @Test
    void レシピ更新_正常系_200が返る() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);
        recipe.setTitle("更新後タイトル");

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeService.updateRecipe(eq(1L), any(UpdateRecipeRequest.class), eq(user)))
                .thenReturn(recipe);

        UpdateRecipeRequest request = UpdateRecipeRequest.builder()
                .title("更新後タイトル")
                .build();

        mockMvc.perform(put("/api/recipes/1")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新後タイトル"));
    }

    @Test
    void サービス設計更新_正常系_200が返る() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeService.updateServiceDesign(eq(1L), any(UpdateServiceDesignRequest.class), eq(user)))
                .thenReturn(recipe);

        UpdateServiceDesignRequest request = UpdateServiceDesignRequest.builder()
                .platingInstructions("白い皿")
                .build();

        mockMvc.perform(put("/api/recipes/1/service-design")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void ステータス変更_正常系_200が返る() throws Exception {
        User user = createChefUser();
        Recipe recipe = createTestRecipe(user);
        recipe.setStatus(RecipeStatus.PUBLISHED);

        when(userService.getUserById(1L)).thenReturn(user);
        when(recipeService.updateStatus(eq(1L), eq(RecipeStatus.PUBLISHED), eq(user)))
                .thenReturn(recipe);

        UpdateStatusRequest request = new UpdateStatusRequest(RecipeStatus.PUBLISHED);

        mockMvc.perform(put("/api/recipes/1/status")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void レシピ削除_正常系_204が返る() throws Exception {
        User user = createChefUser();
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(delete("/api/recipes/1")
                        .with(authentication(chefAuth())))
                .andExpect(status().isNoContent());

        verify(recipeService).deleteRecipe(1L, user);
    }

    @Test
    void 変更履歴取得_正常系_履歴リストが返る() throws Exception {
        User user = createChefUser();
        RecipeHistory history = RecipeHistory.builder()
                .id(1L).changeType("CREATE").changedFields("レシピ新規作成")
                .changedBy(user).changedAt(FIXED_TIME)
                .build();

        when(recipeService.getRecipeHistory(1L)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/recipes/1/history")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].changeType").value("CREATE"))
                .andExpect(jsonPath("$[0].changedByName").value("Chef"));
    }
}
