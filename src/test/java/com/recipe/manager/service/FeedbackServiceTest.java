package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateProductFeedbackRequest;
import com.recipe.manager.entity.CollectionMethod;
import com.recipe.manager.entity.FeedbackSummary;
import com.recipe.manager.entity.ProductFeedback;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.FeedbackSummaryRepository;
import com.recipe.manager.repository.ProductFeedbackRepository;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class FeedbackServiceTest {

    @Mock
    private ProductFeedbackRepository feedbackRepository;

    @Mock
    private FeedbackSummaryRepository summaryRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User chefUser;
    private User serviceUser;
    private User purchaserUser;
    private User producerUser;
    private Recipe recipe;
    private Store store;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(2L).email("service@example.com").name("Service").role(Role.SERVICE).build();
        purchaserUser = User.builder().id(3L).email("purchaser@example.com").name("Purchaser").role(Role.PURCHASER).build();
        producerUser = User.builder().id(4L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        recipe = Recipe.builder().id(1L).title("テストレシピ").status(RecipeStatus.PUBLISHED).createdBy(chefUser).build();
        store = Store.builder().id(1L).storeCode("STORE001").name("本店").build();
    }

    @Test
    void フィードバック作成_正常系_シェフが登録できる() {
        CreateProductFeedbackRequest request = CreateProductFeedbackRequest.builder()
                .recipeId(1L)
                .storeId(1L)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .satisfactionScore(4)
                .emotionScore(5)
                .comment("素晴らしい料理でした")
                .collectionMethod(CollectionMethod.SURVEY)
                .build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(feedbackRepository.save(any(ProductFeedback.class))).thenAnswer(inv -> {
            ProductFeedback f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        ProductFeedback result = feedbackService.createFeedback(request, chefUser);

        assertEquals(4, result.getSatisfactionScore());
        assertEquals(5, result.getEmotionScore());
        assertEquals(CollectionMethod.SURVEY, result.getCollectionMethod());
        verify(feedbackRepository).save(any(ProductFeedback.class));
    }

    @Test
    void フィードバック作成_正常系_サービスが登録できる() {
        CreateProductFeedbackRequest request = CreateProductFeedbackRequest.builder()
                .recipeId(1L)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .satisfactionScore(3)
                .collectionMethod(CollectionMethod.DIRECT)
                .build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(feedbackRepository.save(any(ProductFeedback.class))).thenAnswer(inv -> {
            ProductFeedback f = inv.getArgument(0);
            f.setId(2L);
            return f;
        });

        ProductFeedback result = feedbackService.createFeedback(request, serviceUser);

        assertEquals(3, result.getSatisfactionScore());
        verify(feedbackRepository).save(any(ProductFeedback.class));
    }

    @Test
    void フィードバック作成_異常系_PURCHASERは登録できない() {
        CreateProductFeedbackRequest request = CreateProductFeedbackRequest.builder()
                .recipeId(1L)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .satisfactionScore(4)
                .collectionMethod(CollectionMethod.SURVEY)
                .build();

        assertThrows(ForbiddenException.class,
                () -> feedbackService.createFeedback(request, purchaserUser));
    }

    @Test
    void フィードバック作成_異常系_開始日が終了日より後() {
        CreateProductFeedbackRequest request = CreateProductFeedbackRequest.builder()
                .recipeId(1L)
                .periodStart(LocalDate.of(2026, 2, 1))
                .periodEnd(LocalDate.of(2026, 1, 1))
                .satisfactionScore(4)
                .collectionMethod(CollectionMethod.SURVEY)
                .build();

        assertThrows(BusinessLogicException.class,
                () -> feedbackService.createFeedback(request, chefUser));
    }

    @Test
    void フィードバック作成_異常系_存在しないレシピ() {
        CreateProductFeedbackRequest request = CreateProductFeedbackRequest.builder()
                .recipeId(999L)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .satisfactionScore(4)
                .collectionMethod(CollectionMethod.SURVEY)
                .build();

        when(recipeRepository.findByIdAndStatusNot(999L, RecipeStatus.DELETED)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feedbackService.createFeedback(request, chefUser));
    }

    @Test
    void フィードバック取得_正常系() {
        ProductFeedback feedback = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).registeredBy(chefUser).build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        ProductFeedback result = feedbackService.getFeedbackById(1L);

        assertEquals(4, result.getSatisfactionScore());
    }

    @Test
    void フィードバック取得_異常系_存在しない() {
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feedbackService.getFeedbackById(999L));
    }

    @Test
    void フィードバック一覧_正常系_レシピIDでフィルタ() {
        ProductFeedback feedback = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).registeredBy(chefUser).build();

        when(feedbackRepository.findByRecipeId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(feedback)));

        Page<ProductFeedback> result = feedbackService.listFeedbacks(1L, null, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void フィードバック削除_正常系_自分のフィードバックを削除() {
        ProductFeedback feedback = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).registeredBy(chefUser).build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        feedbackService.deleteFeedback(1L, chefUser);

        verify(feedbackRepository).delete(feedback);
    }

    @Test
    void フィードバック削除_正常系_PRODUCERは他人のフィードバックを削除できる() {
        ProductFeedback feedback = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).registeredBy(chefUser).build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        feedbackService.deleteFeedback(1L, producerUser);

        verify(feedbackRepository).delete(feedback);
    }

    @Test
    void フィードバック削除_異常系_他人のフィードバックは削除できない() {
        ProductFeedback feedback = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).registeredBy(chefUser).build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        assertThrows(ForbiddenException.class,
                () -> feedbackService.deleteFeedback(1L, serviceUser));

        verify(feedbackRepository, never()).delete(any());
    }

    @Test
    void サマリー生成_正常系() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        ProductFeedback fb1 = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(4).emotionScore(5)
                .comment("美味しかった").registeredBy(chefUser).build();
        ProductFeedback fb2 = ProductFeedback.builder()
                .id(2L).recipe(recipe).satisfactionScore(3).emotionScore(4)
                .comment("普通でした").registeredBy(serviceUser).build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(feedbackRepository.countByRecipeIdAndPeriod(1L, start, end)).thenReturn(2L);
        when(feedbackRepository.calculateAvgSatisfaction(1L, start, end)).thenReturn(3.5);
        when(feedbackRepository.calculateAvgEmotion(1L, start, end)).thenReturn(4.5);
        when(feedbackRepository.findByRecipeIdAndPeriod(1L, start, end)).thenReturn(List.of(fb1, fb2));
        when(summaryRepository.findByRecipeIdAndPeriodStartAndPeriodEnd(1L, start, end)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(FeedbackSummary.class))).thenAnswer(inv -> {
            FeedbackSummary s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        FeedbackSummary result = feedbackService.generateSummary(1L, start, end);

        assertEquals(new BigDecimal("3.50"), result.getAvgSatisfaction());
        assertEquals(new BigDecimal("4.50"), result.getAvgEmotion());
        assertEquals(2, result.getFeedbackCount());
        assertNotNull(result.getMainCommentTrend());
    }

    @Test
    void サマリー生成_異常系_フィードバックなし() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(feedbackRepository.countByRecipeIdAndPeriod(1L, start, end)).thenReturn(0L);

        assertThrows(BusinessLogicException.class,
                () -> feedbackService.generateSummary(1L, start, end));
    }

    @Test
    void サマリー生成_正常系_既存サマリーを更新() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        FeedbackSummary existingSummary = FeedbackSummary.builder()
                .id(1L).recipe(recipe).periodStart(start).periodEnd(end)
                .avgSatisfaction(new BigDecimal("3.00")).feedbackCount(1).build();

        ProductFeedback fb = ProductFeedback.builder()
                .id(1L).recipe(recipe).satisfactionScore(5).emotionScore(5)
                .comment("最高！").registeredBy(chefUser).build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(feedbackRepository.countByRecipeIdAndPeriod(1L, start, end)).thenReturn(2L);
        when(feedbackRepository.calculateAvgSatisfaction(1L, start, end)).thenReturn(4.0);
        when(feedbackRepository.calculateAvgEmotion(1L, start, end)).thenReturn(5.0);
        when(feedbackRepository.findByRecipeIdAndPeriod(1L, start, end)).thenReturn(List.of(fb));
        when(summaryRepository.findByRecipeIdAndPeriodStartAndPeriodEnd(1L, start, end))
                .thenReturn(Optional.of(existingSummary));
        when(summaryRepository.save(any(FeedbackSummary.class))).thenAnswer(inv -> inv.getArgument(0));

        FeedbackSummary result = feedbackService.generateSummary(1L, start, end);

        assertEquals(new BigDecimal("4.00"), result.getAvgSatisfaction());
        assertEquals(2, result.getFeedbackCount());
        assertEquals(1L, result.getId());
    }

    @Test
    void サマリー一覧_正常系() {
        FeedbackSummary summary = FeedbackSummary.builder()
                .id(1L).recipe(recipe).periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .avgSatisfaction(new BigDecimal("4.00")).feedbackCount(10).build();

        when(summaryRepository.findByRecipeIdOrderByPeriodStartDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(summary)));

        Page<FeedbackSummary> result = feedbackService.listSummaries(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void サマリートレンド_正常系_時系列で取得() {
        FeedbackSummary jan = FeedbackSummary.builder()
                .id(1L).recipe(recipe)
                .periodStart(LocalDate.of(2026, 1, 1)).periodEnd(LocalDate.of(2026, 1, 31))
                .avgSatisfaction(new BigDecimal("3.50")).feedbackCount(5).build();
        FeedbackSummary feb = FeedbackSummary.builder()
                .id(2L).recipe(recipe)
                .periodStart(LocalDate.of(2026, 2, 1)).periodEnd(LocalDate.of(2026, 2, 28))
                .avgSatisfaction(new BigDecimal("4.20")).feedbackCount(8).build();

        when(recipeRepository.findByIdAndStatusNot(1L, RecipeStatus.DELETED)).thenReturn(Optional.of(recipe));
        when(summaryRepository.findByRecipeIdOrderByPeriodStartAsc(1L)).thenReturn(List.of(jan, feb));

        List<FeedbackSummary> result = feedbackService.getSummaryTrend(1L);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("3.50"), result.get(0).getAvgSatisfaction());
        assertEquals(new BigDecimal("4.20"), result.get(1).getAvgSatisfaction());
    }
}
