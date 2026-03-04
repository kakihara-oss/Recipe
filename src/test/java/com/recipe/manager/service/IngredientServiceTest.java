package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.dto.request.UpdateIngredientSeasonRequest;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.IngredientSeasonRepository;
import com.recipe.manager.repository.RecipeIngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientPriceRepository priceRepository;

    @Mock
    private IngredientSeasonRepository seasonRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    private User purchaserUser;
    private User producerUser;
    private User chefUser;
    private User serviceUser;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        purchaserUser = User.builder().id(1L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        producerUser = User.builder().id(2L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        chefUser = User.builder().id(3L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(4L).email("service@example.com").name("Service").role(Role.SERVICE).build();
        testIngredient = Ingredient.builder()
                .id(1L).name("トマト").category("野菜").standardUnit("個")
                .seasonalFlag(true).supplyStatus(SupplyStatus.AVAILABLE).supplier("農家A")
                .build();
    }

    @Test
    void 食材作成_正常系_PURCHASERが作成できる() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト").category("野菜").standardUnit("個").seasonalFlag(true).supplier("農家A")
                .build();

        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        Ingredient result = ingredientService.createIngredient(request, purchaserUser);

        assertEquals("トマト", result.getName());
        assertEquals("野菜", result.getCategory());
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void 食材作成_正常系_PRODUCERが作成できる() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("バジル").build();

        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(2L);
            return i;
        });

        Ingredient result = ingredientService.createIngredient(request, producerUser);

        assertEquals("バジル", result.getName());
    }

    @Test
    void 食材作成_異常系_CHEFは作成できない() {
        CreateIngredientRequest request = CreateIngredientRequest.builder().name("テスト").build();

        assertThrows(ForbiddenException.class,
                () -> ingredientService.createIngredient(request, chefUser));
    }

    @Test
    void 食材作成_異常系_SERVICEは作成できない() {
        CreateIngredientRequest request = CreateIngredientRequest.builder().name("テスト").build();

        assertThrows(ForbiddenException.class,
                () -> ingredientService.createIngredient(request, serviceUser));
    }

    @Test
    void 食材更新_正常系_名前とカテゴリを更新できる() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateIngredientRequest request = UpdateIngredientRequest.builder()
                .name("ミニトマト").category("野菜・果菜").build();

        Ingredient result = ingredientService.updateIngredient(1L, request, purchaserUser);

        assertEquals("ミニトマト", result.getName());
        assertEquals("野菜・果菜", result.getCategory());
    }

    @Test
    void 食材取得_異常系_存在しない食材() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ingredientService.getIngredientById(999L));
    }

    @Test
    void 食材一覧_正常系_フィルタ付き取得() {
        Pageable pageable = PageRequest.of(0, 20);
        when(ingredientRepository.findByFilters("野菜", null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(testIngredient)));

        Page<Ingredient> result = ingredientService.listIngredients("野菜", null, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("トマト", result.getContent().get(0).getName());
    }

    @Test
    void 供給状態変更_正常系_UNAVAILABLEに変更できる() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = ingredientService.updateSupplyStatus(1L, SupplyStatus.UNAVAILABLE, purchaserUser);

        assertEquals(SupplyStatus.UNAVAILABLE, result.getSupplyStatus());
    }

    @Test
    void 価格追加_正常系_新しい価格を追加できる() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(priceRepository.save(any(IngredientPrice.class))).thenAnswer(inv -> {
            IngredientPrice p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        CreateIngredientPriceRequest request = CreateIngredientPriceRequest.builder()
                .unitPrice(new BigDecimal("150.00"))
                .pricePerUnit("個")
                .effectiveFrom(LocalDate.of(2026, 3, 1))
                .build();

        IngredientPrice result = ingredientService.addPrice(1L, request, purchaserUser);

        assertEquals(new BigDecimal("150.00"), result.getUnitPrice());
        verify(priceRepository).save(any(IngredientPrice.class));
    }

    @Test
    void 旬情報更新_正常系_一括更新できる() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(seasonRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<UpdateIngredientSeasonRequest> requests = List.of(
                UpdateIngredientSeasonRequest.builder().month(6).availabilityRank("HIGH").qualityNote("旬の最盛期").build(),
                UpdateIngredientSeasonRequest.builder().month(7).availabilityRank("HIGH").qualityNote("まだ美味しい").build()
        );

        List<IngredientSeason> result = ingredientService.updateSeasons(1L, requests, purchaserUser);

        assertEquals(2, result.size());
        verify(seasonRepository).deleteByIngredientId(1L);
        verify(seasonRepository).saveAll(any());
    }

    @Test
    void 影響レシピ取得_正常系_食材を使用するレシピ一覧を取得できる() {
        Recipe recipe = Recipe.builder().id(10L).title("トマトパスタ").build();
        RecipeIngredient ri = RecipeIngredient.builder()
                .id(1L).recipe(recipe).ingredient(testIngredient).build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(recipeIngredientRepository.findByIngredientId(1L)).thenReturn(List.of(ri));

        var result = ingredientService.getAffectedRecipes(1L);

        assertEquals(1, result.size());
        assertEquals("トマトパスタ", result.get(0).getRecipeTitle());
    }
}
