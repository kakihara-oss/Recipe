package com.recipe.manager.repository;

import com.recipe.manager.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Page<KnowledgeArticle> findByCategoryId(Long categoryId, Pageable pageable);

    Page<KnowledgeArticle> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR a.tags LIKE %:keyword%")
    List<KnowledgeArticle> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT a FROM KnowledgeArticle a JOIN a.relatedRecipes r WHERE r.id = :recipeId")
    List<KnowledgeArticle> findByRelatedRecipeId(@Param("recipeId") Long recipeId);
}
