package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateIngredientPriceRequest;
import com.recipe.manager.dto.request.CreateIngredientRequest;
import com.recipe.manager.dto.request.CreateIngredientSeasonRequest;
import com.recipe.manager.dto.request.UpdateIngredientRequest;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.IngredientPrice;
import com.recipe.manager.entity.IngredientSeason;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SupplyStatus;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientPriceRepository;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.IngredientSeasonRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientPriceRepository ingredientPriceRepository;

    @Mock
    private IngredientSeasonRepository ingredientSeasonRepository;

    @InjectMocks
    private IngredientService ingredientService;

    private User purchaserUser;
    private User producerUser;
    private User chefUser;
    private User serviceUser;

    @BeforeEach
    void setUp() {
        purchaserUser = User.builder().id(1L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        producerUser = User.builder().id(2L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        chefUser = User.builder().id(3L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(4L).email("service@example.com").name("Service").role(Role.SERVICE).build();
    }

    private Ingredient createTestIngredient() {
        return Ingredient.builder()
                .id(1L)
                .name("トマト")
                .category("野菜")
                .standardUnit("kg")
                .supplyStatus(SupplyStatus.AVAILABLE)
                .supplier("テスト仕入先")
                .build();
    }

    @Test
    void 食材作成_正常系_調達担当が作成できる() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト")
                .category("野菜")
                .standardUnit("kg")
                .supplyStatus(SupplyStatus.AVAILABLE)
                .supplier("テスト仕入先")
                .build();

        when(ingredientRepository.findByName("トマト")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Ingredient result = ingredientService.createIngredient(request, purchaserUser);

        assertNotNull(result);
        assertEquals("トマト", result.getName());
        assertEquals("野菜", result.getCategory());
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void 食材作成_正常系_プロデューサーが作成できる() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("バジル")
                .category("ハーブ")
                .build();

        when(ingredientRepository.findByName("バジル")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Ingredient result = ingredientService.createIngredient(request, producerUser);

        assertNotNull(result);
        assertEquals("バジル", result.getName());
    }

    @Test
    void 食材作成_異常系_シェフは作成できない() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト")
                .build();

        assertThrows(ForbiddenException.class, () ->
                ingredientService.createIngredient(request, chefUser));
    }

    @Test
    void 食材作成_異常系_サービスは作成できない() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト")
                .build();

        assertThrows(ForbiddenException.class, () ->
                ingredientService.createIngredient(request, serviceUser));
    }

    @Test
    void 食材作成_異常系_同名食材が既に存在する() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("トマト")
                .build();

        when(ingredientRepository.findByName("トマト")).thenReturn(Optional.of(createTestIngredient()));

        assertThrows(BusinessLogicException.class, () ->
                ingredientService.createIngredient(request, purchaserUser));
    }

    @Test
    void 食材取得_正常系_IDで取得できる() {
        Ingredient ingredient = createTestIngredient();
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        Ingredient result = ingredientService.getIngredientById(1L);

        assertEquals("トマト", result.getName());
    }

    @Test
    void 食材取得_異常系_存在しないIDで例外が発生する() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ingredientService.getIngredientById(999L));
    }

    @Test
    void 食材一覧_正常系_全件取得できる() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Ingredient> page = new PageImpl<>(List.of(createTestIngredient()));
        when(ingredientRepository.findAll(pageable)).thenReturn(page);

        Page<Ingredient> result = ingredientService.listIngredients(null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void 食材一覧_正常系_カテゴリでフィルタできる() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Ingredient> page = new PageImpl<>(List.of(createTestIngredient()));
        when(ingredientRepository.findByCategory("野菜", pageable)).thenReturn(page);

        Page<Ingredient> result = ingredientService.listIngredients("野菜", null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void 食材一覧_正常系_供給状態でフィルタできる() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Ingredient> page = new PageImpl<>(List.of(createTestIngredient()));
        when(ingredientRepository.findBySupplyStatus(SupplyStatus.AVAILABLE, pageable)).thenReturn(page);

        Page<Ingredient> result = ingredientService.listIngredients(null, SupplyStatus.AVAILABLE, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void 食材更新_正常系_調達担当が更新できる() {
        Ingredient ingredient = createTestIngredient();
        UpdateIngredientRequest request = UpdateIngredientRequest.builder()
                .name("ミニトマト")
                .category("野菜")
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.findByName("ミニトマト")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ingredient result = ingredientService.updateIngredient(1L, request, purchaserUser);

        assertEquals("ミニトマト", result.getName());
    }

    @Test
    void 供給状態変更_正常系_調達担当が変更できる() {
        Ingredient ingredient = createTestIngredient();
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ingredient result = ingredientService.updateSupplyStatus(1L, SupplyStatus.UNAVAILABLE, purchaserUser);

        assertEquals(SupplyStatus.UNAVAILABLE, result.getSupplyStatus());
    }

    @Test
    void 価格追加_正常系_価格を追加できる() {
        Ingredient ingredient = createTestIngredient();
        CreateIngredientPriceRequest request = CreateIngredientPriceRequest.builder()
                .unitPrice(new BigDecimal("300.00"))
                .pricePerUnit("1kg")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientPriceRepository.save(any(IngredientPrice.class))).thenAnswer(invocation -> {
            IngredientPrice saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        IngredientPrice result = ingredientService.addPrice(1L, request, purchaserUser);

        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.getUnitPrice());
    }

    @Test
    void 価格追加_異常系_有効開始日が有効終了日より後() {
        Ingredient ingredient = createTestIngredient();
        CreateIngredientPriceRequest request = CreateIngredientPriceRequest.builder()
                .unitPrice(new BigDecimal("300.00"))
                .effectiveFrom(LocalDate.of(2026, 12, 1))
                .effectiveTo(LocalDate.of(2026, 1, 1))
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        assertThrows(BusinessLogicException.class, () ->
                ingredientService.addPrice(1L, request, purchaserUser));
    }

    @Test
    void 旬情報追加_正常系_旬情報を追加できる() {
        Ingredient ingredient = createTestIngredient();
        CreateIngredientSeasonRequest request = CreateIngredientSeasonRequest.builder()
                .month(8)
                .availabilityRank("A")
                .qualityNote("夏が旬で最も美味しい")
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientSeasonRepository.findByIngredientIdAndMonth(1L, 8)).thenReturn(Optional.empty());
        when(ingredientSeasonRepository.save(any(IngredientSeason.class))).thenAnswer(invocation -> {
            IngredientSeason saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        IngredientSeason result = ingredientService.addSeason(1L, request, purchaserUser);

        assertNotNull(result);
        assertEquals(8, result.getMonth());
        assertEquals("A", result.getAvailabilityRank());
    }

    @Test
    void 旬情報追加_異常系_同月の旬情報が既に存在する() {
        Ingredient ingredient = createTestIngredient();
        CreateIngredientSeasonRequest request = CreateIngredientSeasonRequest.builder()
                .month(8)
                .availabilityRank("A")
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientSeasonRepository.findByIngredientIdAndMonth(1L, 8))
                .thenReturn(Optional.of(IngredientSeason.builder().id(1L).build()));

        assertThrows(BusinessLogicException.class, () ->
                ingredientService.addSeason(1L, request, purchaserUser));
    }

    @Test
    void 旬情報削除_正常系_旬情報を削除できる() {
        Ingredient ingredient = createTestIngredient();
        IngredientSeason season = IngredientSeason.builder()
                .id(1L)
                .ingredient(ingredient)
                .month(8)
                .availabilityRank("A")
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientSeasonRepository.findById(1L)).thenReturn(Optional.of(season));

        ingredientService.deleteSeason(1L, 1L, purchaserUser);

        verify(ingredientSeasonRepository).delete(season);
    }

    @Test
    void 旬情報削除_異常系_異なる食材の旬情報() {
        Ingredient ingredient = createTestIngredient();
        Ingredient otherIngredient = Ingredient.builder().id(2L).name("バジル").build();
        IngredientSeason season = IngredientSeason.builder()
                .id(1L)
                .ingredient(otherIngredient)
                .month(8)
                .build();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientSeasonRepository.findById(1L)).thenReturn(Optional.of(season));

        assertThrows(BusinessLogicException.class, () ->
                ingredientService.deleteSeason(1L, 1L, purchaserUser));
    }
}
