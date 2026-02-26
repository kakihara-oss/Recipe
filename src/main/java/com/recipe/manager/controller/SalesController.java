package com.recipe.manager.controller;

import com.recipe.manager.dto.response.CrossAnalysisResponse;
import com.recipe.manager.dto.response.CsvUploadResponse;
import com.recipe.manager.dto.response.MonthlySalesResponse;
import com.recipe.manager.dto.response.StoreMonthlyFoodCostResponse;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.AnalysisService;
import com.recipe.manager.service.SalesService;
import com.recipe.manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final AnalysisService analysisService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<CsvUploadResponse> uploadPosCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CsvUploadResponse response = salesService.uploadPosCsv(file, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/monthly/{salesMonth}")
    public ResponseEntity<List<MonthlySalesResponse>> getSalesByStoreAndMonth(
            @PathVariable Long storeId,
            @PathVariable String salesMonth) {
        List<MonthlySales> salesList = salesService.getSalesByStoreAndMonth(storeId, salesMonth);
        List<MonthlySalesResponse> response = salesList.stream()
                .map(MonthlySalesResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stores/{storeId}/food-cost/{salesMonth}")
    public ResponseEntity<StoreMonthlyFoodCostResponse> calculateFoodCost(
            @PathVariable Long storeId,
            @PathVariable String salesMonth,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StoreMonthlyFoodCost cost = salesService.calculateTheoreticalFoodCost(storeId, salesMonth, currentUser);
        return ResponseEntity.ok(StoreMonthlyFoodCostResponse.from(cost));
    }

    @GetMapping("/food-cost/comparison/{salesMonth}")
    public ResponseEntity<List<StoreMonthlyFoodCostResponse>> getStoreComparison(
            @PathVariable String salesMonth) {
        List<StoreMonthlyFoodCost> costs = salesService.getStoreComparison(salesMonth);
        List<StoreMonthlyFoodCostResponse> response = costs.stream()
                .map(StoreMonthlyFoodCostResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/food-cost/trend/{storeId}")
    public ResponseEntity<List<StoreMonthlyFoodCostResponse>> getMonthlyTrend(
            @PathVariable Long storeId,
            @RequestParam String fromMonth,
            @RequestParam String toMonth) {
        List<StoreMonthlyFoodCost> costs = salesService.getMonthlyTrend(storeId, fromMonth, toMonth);
        List<StoreMonthlyFoodCostResponse> response = costs.stream()
                .map(StoreMonthlyFoodCostResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/cross/{storeId}/{salesMonth}")
    public ResponseEntity<List<CrossAnalysisResponse>> getCrossAnalysis(
            @PathVariable Long storeId,
            @PathVariable String salesMonth) {
        List<CrossAnalysisResponse> analysis = analysisService.getCrossAnalysis(storeId, salesMonth);
        return ResponseEntity.ok(analysis);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
