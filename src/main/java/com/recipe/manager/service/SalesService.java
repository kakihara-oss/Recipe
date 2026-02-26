package com.recipe.manager.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesService {

    private static final Pattern SALES_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private final MonthlySalesRepository monthlySalesRepository;
    private final StoreMonthlyFoodCostRepository storeMonthlyFoodCostRepository;
    private final StoreRepository storeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeCostRepository recipeCostRepository;

    @Transactional
    public CsvUploadResponse uploadPosCsv(MultipartFile file, User currentUser) {
        validateCsvUploadPermission(currentUser);

        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        Set<String> processedStoreMonths = new HashSet<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            int lineNumber = 0;

            while ((line = reader.readNext()) != null) {
                lineNumber++;

                if (lineNumber == 1 && isHeaderRow(line)) {
                    continue;
                }

                totalRows++;

                if (line.length < 5) {
                    errors.add(String.format("行%d: カラム数が不足しています（5列必要、%d列検出）", lineNumber, line.length));
                    continue;
                }

                String storeCode = line[0].trim();
                String recipeCode = line[1].trim();
                String salesMonth = line[2].trim();
                String quantityStr = line[3].trim();
                String salesAmountStr = line[4].trim();

                if (!SALES_MONTH_PATTERN.matcher(salesMonth).matches()) {
                    errors.add(String.format("行%d: 対象年月のフォーマットが不正です（YYYY-MM）: %s", lineNumber, salesMonth));
                    continue;
                }

                Store store = storeRepository.findByStoreCode(storeCode).orElse(null);
                if (store == null) {
                    errors.add(String.format("行%d: 存在しない店舗コードです: %s", lineNumber, storeCode));
                    continue;
                }

                Recipe recipe;
                try {
                    Long recipeId = Long.parseLong(recipeCode);
                    recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED).orElse(null);
                } catch (NumberFormatException e) {
                    recipe = null;
                }
                if (recipe == null) {
                    errors.add(String.format("行%d: 存在しないレシピコードです: %s", lineNumber, recipeCode));
                    continue;
                }

                int quantity;
                BigDecimal salesAmount;
                try {
                    quantity = Integer.parseInt(quantityStr);
                    salesAmount = new BigDecimal(salesAmountStr);
                } catch (NumberFormatException e) {
                    errors.add(String.format("行%d: 出数または売上金額の数値が不正です", lineNumber));
                    continue;
                }

                String storeMonthKey = store.getId() + "_" + salesMonth;
                if (!processedStoreMonths.contains(storeMonthKey)) {
                    monthlySalesRepository.deleteByStoreIdAndSalesMonth(store.getId(), salesMonth);
                    processedStoreMonths.add(storeMonthKey);
                }

                MonthlySales sales = MonthlySales.builder()
                        .store(store)
                        .recipe(recipe)
                        .salesMonth(salesMonth)
                        .quantity(quantity)
                        .salesAmount(salesAmount)
                        .build();

                monthlySalesRepository.save(sales);
                successRows++;
            }
        } catch (IOException | CsvValidationException e) {
            throw new BusinessLogicException("CSVファイルの読み込みに失敗しました: " + e.getMessage(), e);
        }

        log.info("POS CSV uploaded: totalRows={}, success={}, errors={}, by={}",
                totalRows, successRows, errors.size(), currentUser.getEmail());

        return CsvUploadResponse.builder()
                .totalRows(totalRows)
                .successRows(successRows)
                .errorRows(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MonthlySales> getSalesByStoreAndMonth(Long storeId, String salesMonth) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        return monthlySalesRepository.findByStoreIdAndSalesMonthOrderByRecipeId(storeId, salesMonth);
    }

    @Transactional
    public StoreMonthlyFoodCost calculateTheoreticalFoodCost(Long storeId, String salesMonth, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));

        List<MonthlySales> salesList = monthlySalesRepository.findByStoreIdAndSalesMonth(storeId, salesMonth);
        if (salesList.isEmpty()) {
            throw new BusinessLogicException(
                    String.format("店舗ID=%d、対象月=%sの売上データが存在しません", storeId, salesMonth));
        }

        BigDecimal theoreticalFoodCost = BigDecimal.ZERO;
        BigDecimal totalSales = BigDecimal.ZERO;

        for (MonthlySales sales : salesList) {
            totalSales = totalSales.add(sales.getSalesAmount());

            RecipeCost recipeCost = recipeCostRepository.findByRecipeId(sales.getRecipe().getId()).orElse(null);
            if (recipeCost != null) {
                BigDecimal itemCost = recipeCost.getTotalIngredientCost()
                        .multiply(BigDecimal.valueOf(sales.getQuantity()));
                theoreticalFoodCost = theoreticalFoodCost.add(itemCost);
            }
        }

        BigDecimal costRate = BigDecimal.ZERO;
        if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
            costRate = theoreticalFoodCost
                    .divide(totalSales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        StoreMonthlyFoodCost foodCost = storeMonthlyFoodCostRepository
                .findByStoreIdAndSalesMonth(storeId, salesMonth)
                .orElse(StoreMonthlyFoodCost.builder()
                        .store(store)
                        .salesMonth(salesMonth)
                        .build());

        foodCost.setTheoreticalFoodCost(theoreticalFoodCost.setScale(2, RoundingMode.HALF_UP));
        foodCost.setTotalSales(totalSales.setScale(2, RoundingMode.HALF_UP));
        foodCost.setTheoreticalFoodCostRate(costRate);
        foodCost.setCalculatedAt(LocalDateTime.now());

        StoreMonthlyFoodCost saved = storeMonthlyFoodCostRepository.save(foodCost);
        log.info("Theoretical food cost calculated: storeId={}, month={}, cost={}, rate={}%, by={}",
                storeId, salesMonth, theoreticalFoodCost, costRate, currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<StoreMonthlyFoodCost> getStoreComparison(String salesMonth) {
        return storeMonthlyFoodCostRepository.findBySalesMonth(salesMonth);
    }

    @Transactional(readOnly = true)
    public List<StoreMonthlyFoodCost> getMonthlyTrend(Long storeId, String fromMonth, String toMonth) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        return storeMonthlyFoodCostRepository.findByStoreIdAndMonthRange(storeId, fromMonth, toMonth);
    }

    private boolean isHeaderRow(String[] line) {
        if (line.length == 0) {
            return false;
        }
        String first = line[0].trim();
        // ヘッダー行は日本語のカラム名で始まる、または数値・店舗コードではない文字列
        // 店舗コードは英数字のみで構成されるため、日本語文字を含むかで判定する
        return first.contains("店舗") || first.equalsIgnoreCase("store_code") || first.equalsIgnoreCase("store code");
    }

    private void validateCsvUploadPermission(User user) {
        Role role = user.getRole();
        if (role != Role.SERVICE && role != Role.PURCHASER && role != Role.PRODUCER) {
            throw new ForbiddenException("POSデータのアップロード権限がありません");
        }
    }
}
