package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateKnowledgeArticleRequest;
import com.recipe.manager.dto.request.UpdateKnowledgeArticleRequest;
import com.recipe.manager.dto.response.KnowledgeArticleResponse;
import com.recipe.manager.dto.response.KnowledgeCategoryResponse;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.KnowledgeService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final UserService userService;

    @GetMapping("/categories")
    public ResponseEntity<List<KnowledgeCategoryResponse>> getCategories() {
        List<KnowledgeCategoryResponse> categories = knowledgeService.getAllCategories().stream()
                .map(KnowledgeCategoryResponse::from)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/articles")
    public ResponseEntity<KnowledgeArticleResponse> createArticle(
            @Valid @RequestBody CreateKnowledgeArticleRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        KnowledgeArticle article = knowledgeService.createArticle(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(KnowledgeArticleResponse.from(article));
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<KnowledgeArticleResponse> getArticle(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeService.getArticleById(id);
        return ResponseEntity.ok(KnowledgeArticleResponse.from(article));
    }

    @GetMapping("/articles")
    public ResponseEntity<Page<KnowledgeArticleResponse>> listArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<KnowledgeArticle> articles = knowledgeService.listArticles(categoryId, pageable);
        return ResponseEntity.ok(articles.map(KnowledgeArticleResponse::from));
    }

    @GetMapping("/articles/search")
    public ResponseEntity<List<KnowledgeArticleResponse>> searchArticles(@RequestParam String keyword) {
        List<KnowledgeArticleResponse> articles = knowledgeService.searchArticles(keyword).stream()
                .map(KnowledgeArticleResponse::from)
                .toList();
        return ResponseEntity.ok(articles);
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<KnowledgeArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeArticleRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        KnowledgeArticle article = knowledgeService.updateArticle(id, request, currentUser);
        return ResponseEntity.ok(KnowledgeArticleResponse.from(article));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        knowledgeService.deleteArticle(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
