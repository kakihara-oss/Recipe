package com.recipe.manager.service;

import com.recipe.manager.dto.response.CrossAnalysisResponse;
import com.recipe.manager.entity.FeedbackSummary;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.FeedbackSummaryRepository;
import com.recipe.manager.repository.MonthlySalesRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final BigDecimal HIGH_SATISFACTION_THRESHOLD = new BigDecimal("3.50");
    private static final BigDecimal HIGH_COST_RATE_THRESHOLD = new BigDecimal("35.00");

    private final MonthlySalesRepository monthlySalesRepository;
    private final RecipeCostRepository recipeCostRepository;
    private final FeedbackSummaryRepository feedbackSummaryRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<CrossAnalysisResponse> getCrossAnalysis(Long storeId, String salesMonth) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));

        List<MonthlySales> salesList = monthlySalesRepository
                .findByStoreIdAndSalesMonthOrderByRecipeId(storeId, salesMonth);

        Map<Long, SalesAggregation> aggregation = new LinkedHashMap<>();
        for (MonthlySales sales : salesList) {
            Long recipeId = sales.getRecipe().getId();
            aggregation.computeIfAbsent(recipeId, k -> new SalesAggregation(
                    recipeId, sales.getRecipe().getTitle()));

            SalesAggregation agg = aggregation.get(recipeId);
            agg.totalQuantity += sales.getQuantity();
            agg.totalSalesAmount = agg.totalSalesAmount.add(sales.getSalesAmount());
        }

        List<CrossAnalysisResponse> results = new ArrayList<>();
        for (SalesAggregation agg : aggregation.values()) {
            RecipeCost recipeCost = recipeCostRepository.findByRecipeId(agg.recipeId).orElse(null);
            BigDecimal totalIngredientCost = BigDecimal.ZERO;
            BigDecimal costRate = BigDecimal.ZERO;

            if (recipeCost != null) {
                totalIngredientCost = recipeCost.getTotalIngredientCost()
                        .multiply(BigDecimal.valueOf(agg.totalQuantity));
                if (agg.totalSalesAmount.compareTo(BigDecimal.ZERO) > 0) {
                    costRate = totalIngredientCost
                            .divide(agg.totalSalesAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            List<FeedbackSummary> summaries = feedbackSummaryRepository
                    .findByRecipeIdOrderByPeriodStartAsc(agg.recipeId);

            BigDecimal avgSatisfaction = null;
            BigDecimal avgEmotion = null;
            if (!summaries.isEmpty()) {
                FeedbackSummary latest = summaries.get(summaries.size() - 1);
                avgSatisfaction = latest.getAvgSatisfaction();
                avgEmotion = latest.getAvgEmotion();
            }

            String insight = generateInsight(avgSatisfaction, avgEmotion, costRate);

            results.add(CrossAnalysisResponse.builder()
                    .recipeId(agg.recipeId)
                    .recipeTitle(agg.recipeTitle)
                    .avgSatisfaction(avgSatisfaction)
                    .avgEmotion(avgEmotion)
                    .totalSalesAmount(agg.totalSalesAmount.setScale(2, RoundingMode.HALF_UP))
                    .totalQuantity(agg.totalQuantity)
                    .totalIngredientCost(totalIngredientCost.setScale(2, RoundingMode.HALF_UP))
                    .theoreticalFoodCostRate(costRate)
                    .insight(insight)
                    .build());
        }

        return results;
    }

    private String generateInsight(BigDecimal avgSatisfaction, BigDecimal avgEmotion, BigDecimal costRate) {
        boolean highSatisfaction = avgSatisfaction != null
                && avgSatisfaction.compareTo(HIGH_SATISFACTION_THRESHOLD) >= 0;
        boolean highCostRate = costRate.compareTo(HIGH_COST_RATE_THRESHOLD) > 0;

        if (highSatisfaction && !highCostRate) {
            return "高満足度・低原価率：収益性と顧客満足度のバランスが優れています";
        }
        if (highSatisfaction && highCostRate) {
            return "高満足度・高原価率：感動度は高いが収益性に課題があります。売価見直しまたは食材コスト削減を検討してください";
        }
        if (!highSatisfaction && !highCostRate) {
            return "低満足度・低原価率：原価率は適正ですが、品質・サービス面の改善を検討してください";
        }
        // !highSatisfaction && highCostRate
        return "低満足度・高原価率：収益性・満足度ともに課題があります。レシピの抜本的な見直しを検討してください";
    }

    private static class SalesAggregation {
        final Long recipeId;
        final String recipeTitle;
        int totalQuantity;
        BigDecimal totalSalesAmount;

        SalesAggregation(Long recipeId, String recipeTitle) {
            this.recipeId = recipeId;
            this.recipeTitle = recipeTitle;
            this.totalQuantity = 0;
            this.totalSalesAmount = BigDecimal.ZERO;
        }
    }
}
