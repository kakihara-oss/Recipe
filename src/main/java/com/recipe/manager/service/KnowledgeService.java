package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateKnowledgeArticleRequest;
import com.recipe.manager.dto.request.UpdateKnowledgeArticleRequest;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.KnowledgeCategory;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.KnowledgeArticleRepository;
import com.recipe.manager.repository.KnowledgeCategoryRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<KnowledgeCategory> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional
    public KnowledgeArticle createArticle(CreateKnowledgeArticleRequest request, User currentUser) {
        KnowledgeCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeCategory", request.getCategoryId()));

        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(category)
                .tags(request.getTags())
                .author(currentUser)
                .build();

        if (request.getRelatedRecipeIds() != null) {
            List<Recipe> recipes = resolveRecipes(request.getRelatedRecipeIds());
            article.setRelatedRecipes(recipes);
        }

        KnowledgeArticle saved = articleRepository.save(article);
        log.info("Knowledge article created: id={}, title={}, by={}",
                saved.getId(), saved.getTitle(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public KnowledgeArticle getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", id));
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeArticle> listArticles(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return articleRepository.findByCategoryId(categoryId, pageable);
        }
        return articleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticle> searchArticles(String keyword) {
        return articleRepository.searchByKeyword(keyword);
    }

    @Transactional
    public KnowledgeArticle updateArticle(Long id, UpdateKnowledgeArticleRequest request, User currentUser) {
        KnowledgeArticle article = getArticleById(id);
        validateArticleEditPermission(article, currentUser);

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getCategoryId() != null) {
            KnowledgeCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("KnowledgeCategory", request.getCategoryId()));
            article.setCategory(category);
        }
        if (request.getTags() != null) {
            article.setTags(request.getTags());
        }
        if (request.getRelatedRecipeIds() != null) {
            List<Recipe> recipes = resolveRecipes(request.getRelatedRecipeIds());
            article.setRelatedRecipes(recipes);
        }

        KnowledgeArticle saved = articleRepository.save(article);
        log.info("Knowledge article updated: id={}, by={}", id, currentUser.getEmail());
        return saved;
    }

    @Transactional
    public void deleteArticle(Long id, User currentUser) {
        KnowledgeArticle article = getArticleById(id);
        validateArticleEditPermission(article, currentUser);
        articleRepository.delete(article);
        log.info("Knowledge article deleted: id={}, by={}", id, currentUser.getEmail());
    }

    private void validateArticleEditPermission(KnowledgeArticle article, User currentUser) {
        if (currentUser.getRole() == Role.PRODUCER) {
            return;
        }
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("他のユーザーのナレッジ記事を編集する権限がありません");
        }
    }

    private List<Recipe> resolveRecipes(List<Long> recipeIds) {
        List<Recipe> recipes = new ArrayList<>();
        for (Long recipeId : recipeIds) {
            Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                    .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
            recipes.add(recipe);
        }
        return recipes;
    }
}
