package com.recipe.manager.service;

import com.recipe.manager.dto.response.CsvUploadResponse;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeCost;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.MonthlySalesRepository;
import com.recipe.manager.repository.RecipeCostRepository;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.repository.StoreMonthlyFoodCostRepository;
import com.recipe.manager.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesService {

    private final MonthlySalesRepository monthlySalesRepository;
    private final StoreMonthlyFoodCostRepository storeMonthlyFoodCostRepository;
    private final StoreRepository storeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeCostRepository recipeCostRepository;

    @Transactional
    public CsvUploadResponse uploadPosCsv(MultipartFile file, User currentUser) {
        validateUploadPermission(currentUser);

        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines and header
                if (line.isBlank() || (lineNumber == 1 && isHeaderLine(line))) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length < 5) {
                    errors.add("行" + lineNumber + ": カラム数が不足しています（5列必要）");
                    skippedCount++;
                    continue;
                }

                String storeCode = fields[0].trim();
                String recipeCode = fields[1].trim();
                String targetMonth = fields[2].trim();
                String quantityStr = fields[3].trim();
                String salesAmountStr = fields[4].trim();

                // Validate target month format
                if (!targetMonth.matches("\\d{4}-\\d{2}")) {
                    errors.add("行" + lineNumber + ": 対象年月の形式が不正です（YYYY-MM）: " + targetMonth);
                    skippedCount++;
                    continue;
                }

                // Lookup store
                Store store = storeRepository.findByStoreCode(storeCode).orElse(null);
                if (store == null) {
                    errors.add("行" + lineNumber + ": 店舗コードが見つかりません: " + storeCode);
                    skippedCount++;
                    continue;
                }

                // Lookup recipe by ID
                Long recipeId;
                try {
                    recipeId = Long.parseLong(recipeCode);
                } catch (NumberFormatException e) {
                    errors.add("行" + lineNumber + ": レシピコードが不正です: " + recipeCode);
                    skippedCount++;
                    continue;
                }

                Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED).orElse(null);
                if (recipe == null) {
                    errors.add("行" + lineNumber + ": レシピが見つかりません: " + recipeCode);
                    skippedCount++;
                    continue;
                }

                // Parse quantity and amount
                int quantity;
                BigDecimal salesAmount;
                try {
                    quantity = Integer.parseInt(quantityStr);
                    salesAmount = new BigDecimal(salesAmountStr);
                } catch (NumberFormatException e) {
                    errors.add("行" + lineNumber + ": 数値の形式が不正です");
                    skippedCount++;
                    continue;
                }

                // Upsert: find existing or create new
                MonthlySales sales = monthlySalesRepository
                        .findByStoreIdAndRecipeIdAndTargetMonth(store.getId(), recipe.getId(), targetMonth)
                        .orElse(MonthlySales.builder()
                                .store(store)
                                .recipe(recipe)
                                .targetMonth(targetMonth)
                                .build());

                sales.setQuantity(quantity);
                sales.setSalesAmount(salesAmount);
                monthlySalesRepository.save(sales);
                importedCount++;
            }
        } catch (Exception e) {
            throw new BusinessLogicException("CSVファイルの読み込みに失敗しました: " + e.getMessage());
        }

        log.info("POS CSV uploaded: imported={}, skipped={}, errors={}, by={}",
                importedCount, skippedCount, errors.size(), currentUser.getEmail());

        return CsvUploadResponse.builder()
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .errors(errors)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MonthlySales> getSalesByStoreAndMonth(Long storeId, String targetMonth) {
        return monthlySalesRepository.findByStoreIdAndTargetMonth(storeId, targetMonth);
    }

    @Transactional(readOnly = true)
    public List<MonthlySales> getSalesByStoreAndRange(Long storeId, String startMonth, String endMonth) {
        return monthlySalesRepository.findByStoreIdAndMonthRange(storeId, startMonth, endMonth);
    }

    @Transactional
    public StoreMonthlyFoodCost calculateMonthlyFoodCost(Long storeId, String targetMonth) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));

        List<MonthlySales> salesList = monthlySalesRepository
                .findByStoreIdAndTargetMonth(storeId, targetMonth);

        if (salesList.isEmpty()) {
            throw new BusinessLogicException("対象月の売上データがありません: " + targetMonth);
        }

        BigDecimal totalTheoreticalCost = BigDecimal.ZERO;
        BigDecimal totalSales = BigDecimal.ZERO;

        for (MonthlySales ms : salesList) {
            totalSales = totalSales.add(ms.getSalesAmount());

            RecipeCost recipeCost = recipeCostRepository.findByRecipeId(ms.getRecipe().getId())
                    .orElse(null);
            if (recipeCost != null && recipeCost.getTotalIngredientCost() != null) {
                BigDecimal itemCost = recipeCost.getTotalIngredientCost()
                        .multiply(BigDecimal.valueOf(ms.getQuantity()));
                totalTheoreticalCost = totalTheoreticalCost.add(itemCost);
            }
        }

        BigDecimal costRate = BigDecimal.ZERO;
        if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
            costRate = totalTheoreticalCost
                    .divide(totalSales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        StoreMonthlyFoodCost foodCost = storeMonthlyFoodCostRepository
                .findByStoreIdAndTargetMonth(storeId, targetMonth)
                .orElse(StoreMonthlyFoodCost.builder()
                        .store(store)
                        .targetMonth(targetMonth)
                        .build());

        foodCost.setTheoreticalFoodCost(totalTheoreticalCost);
        foodCost.setTotalSales(totalSales);
        foodCost.setTheoreticalFoodCostRate(costRate);

        StoreMonthlyFoodCost saved = storeMonthlyFoodCostRepository.save(foodCost);
        log.info("Monthly food cost calculated: storeId={}, month={}, cost={}, rate={}%",
                storeId, targetMonth, totalTheoreticalCost, costRate);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<StoreMonthlyFoodCost> getStoreComparison(String targetMonth) {
        return storeMonthlyFoodCostRepository.findByTargetMonth(targetMonth);
    }

    @Transactional(readOnly = true)
    public List<StoreMonthlyFoodCost> getStoreTrend(Long storeId, String startMonth, String endMonth) {
        return storeMonthlyFoodCostRepository.findByStoreIdAndMonthRange(storeId, startMonth, endMonth);
    }

    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("store_code") || lower.startsWith("店舗") || lower.startsWith("store code");
    }

    private void validateUploadPermission(User user) {
        Role role = user.getRole();
        if (role != Role.SERVICE && role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("POSデータのアップロード権限がありません");
        }
    }
}
