package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateRecipeRequest;
import com.recipe.manager.dto.request.UpdateExperienceDesignRequest;
import com.recipe.manager.dto.request.UpdateRecipeRequest;
import com.recipe.manager.dto.request.UpdateServiceDesignRequest;
import com.recipe.manager.entity.ExperienceDesign;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeHistory;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.ServiceDesign;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.RecipeHistoryRepository;
import com.recipe.manager.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeHistoryRepository recipeHistoryRepository;

    @InjectMocks
    private RecipeService recipeService;

    private User chefUser;
    private User serviceUser;
    private User purchaserUser;
    private User producerUser;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(2L).email("service@example.com").name("Service").role(Role.SERVICE).build();
        purchaserUser = User.builder().id(3L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        producerUser = User.builder().id(4L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
    }

    private Recipe createTestRecipe() {
        return Recipe.builder()
                .id(1L)
                .title("テストレシピ")
                .description("テスト説明")
                .category("メイン")
                .servings(4)
                .status(RecipeStatus.DRAFT)
                .createdBy(chefUser)
                .build();
    }

    @Test
    void レシピ作成_正常系_シェフが作成できる() {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .title("新しいレシピ")
                .description("美味しい料理")
                .category("メイン")
                .servings(4)
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        Recipe result = recipeService.createRecipe(request, chefUser);

        assertEquals("新しいレシピ", result.getTitle());
        assertEquals(RecipeStatus.DRAFT, result.getStatus());
        assertEquals(chefUser, result.getCreatedBy());
        verify(recipeRepository).save(any(Recipe.class));
        verify(recipeHistoryRepository).save(any(RecipeHistory.class));
    }

    @Test
    void レシピ作成_正常系_PRODUCERも作成できる() {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .title("プロデューサーのレシピ")
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        Recipe result = recipeService.createRecipe(request, producerUser);

        assertEquals("プロデューサーのレシピ", result.getTitle());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void レシピ作成_正常系_食材付きで作成できる() {
        Ingredient ingredient = Ingredient.builder().id(1L).name("トマト").build();

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .title("トマトパスタ")
                .ingredients(List.of(
                        CreateRecipeRequest.IngredientInput.builder()
                                .ingredientId(1L)
                                .quantity(BigDecimal.valueOf(200))
                                .unit("g")
                                .build()
                ))
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        Recipe result = recipeService.createRecipe(request, chefUser);

        assertEquals(1, result.getIngredients().size());
        verify(ingredientRepository).findById(1L);
    }

    @Test
    void レシピ作成_異常系_SERVICEは作成できない() {
        CreateRecipeRequest request = CreateRecipeRequest.builder().title("テスト").build();

        assertThrows(ForbiddenException.class,
                () -> recipeService.createRecipe(request, serviceUser));
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void レシピ作成_異常系_PURCHASERは作成できない() {
        CreateRecipeRequest request = CreateRecipeRequest.builder().title("テスト").build();

        assertThrows(ForbiddenException.class,
                () -> recipeService.createRecipe(request, purchaserUser));
    }

    @Test
    void レシピ取得_正常系_IDで取得できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));

        Recipe result = recipeService.getRecipeById(1L);

        assertEquals("テストレシピ", result.getTitle());
    }

    @Test
    void レシピ取得_異常系_存在しないID() {
        when(recipeRepository.findByIdAndStatusNot(999L, RecipeStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recipeService.getRecipeById(999L));
    }

    @Test
    void レシピ一覧_正常系_DELETED以外が取得できる() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> page = new PageImpl<>(List.of(createTestRecipe()));
        when(recipeRepository.findByStatusNot(RecipeStatus.DELETED, pageable))
                .thenReturn(page);

        Page<Recipe> result = recipeService.listRecipes(null, null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void レシピ一覧_正常系_ステータスフィルタ() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> page = new PageImpl<>(List.of(createTestRecipe()));
        when(recipeRepository.findByStatus(RecipeStatus.DRAFT, pageable))
                .thenReturn(page);

        Page<Recipe> result = recipeService.listRecipes(null, RecipeStatus.DRAFT, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void レシピ一覧_正常系_カテゴリフィルタ() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> page = new PageImpl<>(List.of(createTestRecipe()));
        when(recipeRepository.findByStatusNotAndCategory(RecipeStatus.DELETED, "メイン", pageable))
                .thenReturn(page);

        Page<Recipe> result = recipeService.listRecipes("メイン", null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void レシピ一覧_異常系_DELETEDステータスでの検索は不可() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(BusinessLogicException.class,
                () -> recipeService.listRecipes(null, RecipeStatus.DELETED, pageable));
    }

    @Test
    void レシピ更新_正常系_シェフがタイトルを変更できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRecipeRequest request = UpdateRecipeRequest.builder()
                .title("更新後タイトル")
                .build();

        Recipe result = recipeService.updateRecipe(1L, request, chefUser);

        assertEquals("更新後タイトル", result.getTitle());

        ArgumentCaptor<RecipeHistory> captor = ArgumentCaptor.forClass(RecipeHistory.class);
        verify(recipeHistoryRepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getChangeType());
        assertEquals("title", captor.getValue().getChangedFields());
    }

    @Test
    void レシピ更新_異常系_SERVICEは基本情報を更新できない() {
        assertThrows(ForbiddenException.class,
                () -> recipeService.updateRecipe(1L,
                        UpdateRecipeRequest.builder().title("テスト").build(),
                        serviceUser));
    }

    @Test
    void サービス設計更新_正常系_SERVICEが更新できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateServiceDesignRequest request = UpdateServiceDesignRequest.builder()
                .platingInstructions("白い皿に盛り付け")
                .customerScript("本日のおすすめです")
                .build();

        Recipe result = recipeService.updateServiceDesign(1L, request, serviceUser);

        assertNotNull(result.getServiceDesign());
        assertEquals("白い皿に盛り付け", result.getServiceDesign().getPlatingInstructions());
        verify(recipeHistoryRepository).save(any(RecipeHistory.class));
    }

    @Test
    void サービス設計更新_正常系_CHEFも更新できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateServiceDesignRequest request = UpdateServiceDesignRequest.builder()
                .serviceMethod("テーブルサイドで仕上げ")
                .build();

        Recipe result = recipeService.updateServiceDesign(1L, request, chefUser);

        assertNotNull(result.getServiceDesign());
    }

    @Test
    void サービス設計更新_異常系_PURCHASERは更新できない() {
        assertThrows(ForbiddenException.class,
                () -> recipeService.updateServiceDesign(1L,
                        UpdateServiceDesignRequest.builder().build(),
                        purchaserUser));
    }

    @Test
    void 体験設計更新_正常系_SERVICEが更新できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateExperienceDesignRequest request = UpdateExperienceDesignRequest.builder()
                .targetScene("記念日ディナー")
                .emotionalKeyPoints("感動のサプライズ")
                .build();

        Recipe result = recipeService.updateExperienceDesign(1L, request, serviceUser);

        assertNotNull(result.getExperienceDesign());
        assertEquals("記念日ディナー", result.getExperienceDesign().getTargetScene());
    }

    @Test
    void ステータス変更_正常系_DRAFTからPUBLISHED() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.updateStatus(1L, RecipeStatus.PUBLISHED, chefUser);

        assertEquals(RecipeStatus.PUBLISHED, result.getStatus());

        ArgumentCaptor<RecipeHistory> captor = ArgumentCaptor.forClass(RecipeHistory.class);
        verify(recipeHistoryRepository).save(captor.capture());
        assertEquals("STATUS_CHANGE", captor.getValue().getChangeType());
        assertEquals("DRAFT -> PUBLISHED", captor.getValue().getChangedFields());
    }

    @Test
    void ステータス変更_正常系_PUBLISHEDからARCHIVED() {
        Recipe recipe = createTestRecipe();
        recipe.setStatus(RecipeStatus.PUBLISHED);
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.updateStatus(1L, RecipeStatus.ARCHIVED, chefUser);

        assertEquals(RecipeStatus.ARCHIVED, result.getStatus());
    }

    @Test
    void ステータス変更_正常系_ARCHIVEDからPUBLISHED復元() {
        Recipe recipe = createTestRecipe();
        recipe.setStatus(RecipeStatus.ARCHIVED);
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.updateStatus(1L, RecipeStatus.PUBLISHED, chefUser);

        assertEquals(RecipeStatus.PUBLISHED, result.getStatus());
    }

    @Test
    void ステータス変更_異常系_DRAFTからARCHIVEDは不可() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));

        assertThrows(BusinessLogicException.class,
                () -> recipeService.updateStatus(1L, RecipeStatus.ARCHIVED, chefUser));
    }

    @Test
    void ステータス変更_異常系_DELETEDへの直接変更は不可() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));

        assertThrows(BusinessLogicException.class,
                () -> recipeService.updateStatus(1L, RecipeStatus.DELETED, chefUser));
    }

    @Test
    void レシピ削除_正常系_論理削除される() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recipeService.deleteRecipe(1L, chefUser);

        assertEquals(RecipeStatus.DELETED, recipe.getStatus());
        verify(recipeHistoryRepository).save(any(RecipeHistory.class));
    }

    @Test
    void レシピ削除_異常系_SERVICEは削除できない() {
        assertThrows(ForbiddenException.class,
                () -> recipeService.deleteRecipe(1L, serviceUser));
    }

    @Test
    void 変更履歴取得_正常系_レシピの履歴を取得できる() {
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED))
                .thenReturn(Optional.of(recipe));

        RecipeHistory history1 = RecipeHistory.builder()
                .id(1L).recipe(recipe).changedBy(chefUser)
                .changeType("CREATE").changedFields("レシピ新規作成").build();
        RecipeHistory history2 = RecipeHistory.builder()
                .id(2L).recipe(recipe).changedBy(chefUser)
                .changeType("UPDATE").changedFields("title").build();

        when(recipeHistoryRepository.findByRecipeIdOrderByChangedAtDesc(1L))
                .thenReturn(List.of(history2, history1));

        List<RecipeHistory> result = recipeService.getRecipeHistory(1L);

        assertEquals(2, result.size());
        assertEquals("UPDATE", result.get(0).getChangeType());
    }
}
