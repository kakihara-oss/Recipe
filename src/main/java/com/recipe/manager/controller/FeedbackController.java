package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateProductFeedbackRequest;
import com.recipe.manager.dto.request.GenerateFeedbackSummaryRequest;
import com.recipe.manager.dto.response.FeedbackSummaryResponse;
import com.recipe.manager.dto.response.ProductFeedbackResponse;
import com.recipe.manager.entity.FeedbackSummary;
import com.recipe.manager.entity.ProductFeedback;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.FeedbackService;
import com.recipe.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProductFeedbackResponse> createFeedback(
            @Valid @RequestBody CreateProductFeedbackRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        ProductFeedback feedback = feedbackService.createFeedback(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductFeedbackResponse.from(feedback));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductFeedbackResponse> getFeedback(@PathVariable Long id) {
        ProductFeedback feedback = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(ProductFeedbackResponse.from(feedback));
    }

    @GetMapping
    public ResponseEntity<Page<ProductFeedbackResponse>> listFeedbacks(
            @RequestParam(required = false) Long recipeId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductFeedback> feedbacks = feedbackService.listFeedbacks(recipeId, storeId, pageable);
        return ResponseEntity.ok(feedbacks.map(ProductFeedbackResponse::from));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        feedbackService.deleteFeedback(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/summaries/generate")
    public ResponseEntity<FeedbackSummaryResponse> generateSummary(
            @Valid @RequestBody GenerateFeedbackSummaryRequest request) {
        FeedbackSummary summary = feedbackService.generateSummary(
                request.getRecipeId(), request.getPeriodStart(), request.getPeriodEnd());
        return ResponseEntity.status(HttpStatus.CREATED).body(FeedbackSummaryResponse.from(summary));
    }

    @GetMapping("/summaries")
    public ResponseEntity<Page<FeedbackSummaryResponse>> listSummaries(
            @RequestParam Long recipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FeedbackSummary> summaries = feedbackService.listSummaries(recipeId, pageable);
        return ResponseEntity.ok(summaries.map(FeedbackSummaryResponse::from));
    }

    @GetMapping("/summaries/{id}")
    public ResponseEntity<FeedbackSummaryResponse> getSummary(@PathVariable Long id) {
        FeedbackSummary summary = feedbackService.getSummaryById(id);
        return ResponseEntity.ok(FeedbackSummaryResponse.from(summary));
    }

    @GetMapping("/summaries/trend")
    public ResponseEntity<List<FeedbackSummaryResponse>> getSummaryTrend(@RequestParam Long recipeId) {
        List<FeedbackSummaryResponse> trend = feedbackService.getSummaryTrend(recipeId).stream()
                .map(FeedbackSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(trend);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
