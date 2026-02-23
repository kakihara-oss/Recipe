package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateProductFeedbackRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    private final ProductFeedbackRepository feedbackRepository;
    private final FeedbackSummaryRepository summaryRepository;
    private final RecipeRepository recipeRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ProductFeedback createFeedback(CreateProductFeedbackRequest request, User currentUser) {
        validateFeedbackRegistrationPermission(currentUser);
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());

        Recipe recipe = recipeRepository.findByIdAndStatusNot(request.getRecipeId(), RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", request.getRecipeId()));

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store", request.getStoreId()));
        }

        ProductFeedback feedback = ProductFeedback.builder()
                .recipe(recipe)
                .store(store)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .satisfactionScore(request.getSatisfactionScore())
                .emotionScore(request.getEmotionScore())
                .comment(request.getComment())
                .collectionMethod(request.getCollectionMethod())
                .registeredBy(currentUser)
                .build();

        ProductFeedback saved = feedbackRepository.save(feedback);
        log.info("Product feedback created: id={}, recipeId={}, by={}",
                saved.getId(), recipe.getId(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public ProductFeedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductFeedback", id));
    }

    @Transactional(readOnly = true)
    public Page<ProductFeedback> listFeedbacks(Long recipeId, Long storeId, Pageable pageable) {
        if (recipeId != null && storeId != null) {
            return feedbackRepository.findByRecipeIdAndStoreId(recipeId, storeId, pageable);
        }
        if (recipeId != null) {
            return feedbackRepository.findByRecipeId(recipeId, pageable);
        }
        if (storeId != null) {
            return feedbackRepository.findByStoreId(storeId, pageable);
        }
        return feedbackRepository.findAll(pageable);
    }

    @Transactional
    public void deleteFeedback(Long id, User currentUser) {
        ProductFeedback feedback = getFeedbackById(id);
        validateFeedbackDeletePermission(feedback, currentUser);
        feedbackRepository.delete(feedback);
        log.info("Product feedback deleted: id={}, by={}", id, currentUser.getEmail());
    }

    @Transactional
    public FeedbackSummary generateSummary(Long recipeId, LocalDate periodStart, LocalDate periodEnd) {
        validatePeriod(periodStart, periodEnd);

        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        Long count = feedbackRepository.countByRecipeIdAndPeriod(recipeId, periodStart, periodEnd);
        if (count == 0) {
            throw new BusinessLogicException("指定された期間にフィードバックデータがありません");
        }

        Double avgSatisfaction = feedbackRepository.calculateAvgSatisfaction(recipeId, periodStart, periodEnd);
        Double avgEmotion = feedbackRepository.calculateAvgEmotion(recipeId, periodStart, periodEnd);

        List<ProductFeedback> feedbacks = feedbackRepository.findByRecipeIdAndPeriod(recipeId, periodStart, periodEnd);
        String commentTrend = buildCommentTrend(feedbacks);

        FeedbackSummary summary = summaryRepository
                .findByRecipeIdAndPeriodStartAndPeriodEnd(recipeId, periodStart, periodEnd)
                .orElse(FeedbackSummary.builder()
                        .recipe(recipe)
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .build());

        summary.setAvgSatisfaction(BigDecimal.valueOf(avgSatisfaction).setScale(2, RoundingMode.HALF_UP));
        summary.setAvgEmotion(avgEmotion != null
                ? BigDecimal.valueOf(avgEmotion).setScale(2, RoundingMode.HALF_UP)
                : null);
        summary.setFeedbackCount(count.intValue());
        summary.setMainCommentTrend(commentTrend);

        FeedbackSummary saved = summaryRepository.save(summary);
        log.info("Feedback summary generated: id={}, recipeId={}, period={}-{}, count={}",
                saved.getId(), recipeId, periodStart, periodEnd, count);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<FeedbackSummary> listSummaries(Long recipeId, Pageable pageable) {
        return summaryRepository.findByRecipeIdOrderByPeriodStartDesc(recipeId, pageable);
    }

    @Transactional(readOnly = true)
    public FeedbackSummary getSummaryById(Long id) {
        return summaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackSummary", id));
    }

    @Transactional(readOnly = true)
    public List<FeedbackSummary> getSummaryTrend(Long recipeId) {
        recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        return summaryRepository.findByRecipeIdOrderByPeriodStartAsc(recipeId);
    }

    private void validateFeedbackRegistrationPermission(User user) {
        if (user.getRole() == Role.PURCHASER) {
            throw new ForbiddenException("食材調達ロールではフィードバックを登録できません");
        }
    }

    private void validateFeedbackDeletePermission(ProductFeedback feedback, User user) {
        if (user.getRole() == Role.PRODUCER) {
            return;
        }
        if (!feedback.getRegisteredBy().getId().equals(user.getId())) {
            throw new ForbiddenException("他のユーザーのフィードバックを削除する権限がありません");
        }
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessLogicException("評価期間の開始日は終了日より前でなければなりません");
        }
    }

    private String buildCommentTrend(List<ProductFeedback> feedbacks) {
        List<String> comments = feedbacks.stream()
                .filter(f -> f.getComment() != null && !f.getComment().isBlank())
                .map(ProductFeedback::getComment)
                .toList();

        if (comments.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("コメント件数: ").append(comments.size()).append("件\n");
        sb.append("主なコメント:\n");
        int limit = Math.min(comments.size(), 5);
        for (int i = 0; i < limit; i++) {
            String comment = comments.get(i);
            if (comment.length() > 100) {
                comment = comment.substring(0, 100) + "...";
            }
            sb.append("- ").append(comment).append("\n");
        }
        return sb.toString();
    }
}
