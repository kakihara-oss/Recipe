package com.recipe.manager.controller;

import com.recipe.manager.dto.response.CsvUploadResponse;
import com.recipe.manager.dto.response.MonthlySalesResponse;
import com.recipe.manager.dto.response.StoreMonthlyFoodCostResponse;
import com.recipe.manager.entity.MonthlySales;
import com.recipe.manager.entity.StoreMonthlyFoodCost;
import com.recipe.manager.entity.User;
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
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<CsvUploadResponse> uploadPosCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CsvUploadResponse response = salesService.uploadPosCsv(file, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/monthly")
    public ResponseEntity<List<MonthlySalesResponse>> getMonthlySales(
            @PathVariable Long storeId,
            @RequestParam String targetMonth) {
        List<MonthlySales> sales = salesService.getSalesByStoreAndMonth(storeId, targetMonth);
        List<MonthlySalesResponse> response = sales.stream()
                .map(MonthlySalesResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/range")
    public ResponseEntity<List<MonthlySalesResponse>> getSalesByRange(
            @PathVariable Long storeId,
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<MonthlySales> sales = salesService.getSalesByStoreAndRange(storeId, startMonth, endMonth);
        List<MonthlySalesResponse> response = sales.stream()
                .map(MonthlySalesResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stores/{storeId}/food-cost/calculate")
    public ResponseEntity<StoreMonthlyFoodCostResponse> calculateFoodCost(
            @PathVariable Long storeId,
            @RequestParam String targetMonth) {
        StoreMonthlyFoodCost fc = salesService.calculateMonthlyFoodCost(storeId, targetMonth);
        return ResponseEntity.ok(StoreMonthlyFoodCostResponse.from(fc));
    }

    @GetMapping("/food-cost/comparison")
    public ResponseEntity<List<StoreMonthlyFoodCostResponse>> getStoreComparison(
            @RequestParam String targetMonth) {
        List<StoreMonthlyFoodCost> list = salesService.getStoreComparison(targetMonth);
        List<StoreMonthlyFoodCostResponse> response = list.stream()
                .map(StoreMonthlyFoodCostResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/food-cost/trend")
    public ResponseEntity<List<StoreMonthlyFoodCostResponse>> getStoreTrend(
            @PathVariable Long storeId,
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<StoreMonthlyFoodCost> list = salesService.getStoreTrend(storeId, startMonth, endMonth);
        List<StoreMonthlyFoodCostResponse> response = list.stream()
                .map(StoreMonthlyFoodCostResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
