package com.recipe.manager.repository;

import com.recipe.manager.entity.ProductFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductFeedbackRepository extends JpaRepository<ProductFeedback, Long> {

    Page<ProductFeedback> findByRecipeId(Long recipeId, Pageable pageable);

    Page<ProductFeedback> findByStoreId(Long storeId, Pageable pageable);

    Page<ProductFeedback> findByRecipeIdAndStoreId(Long recipeId, Long storeId, Pageable pageable);

    @Query("SELECT f FROM ProductFeedback f WHERE f.recipe.id = :recipeId " +
            "AND f.periodStart >= :from AND f.periodEnd <= :to")
    List<ProductFeedback> findByRecipeIdAndPeriod(@Param("recipeId") Long recipeId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    @Query("SELECT AVG(f.satisfactionScore) FROM ProductFeedback f WHERE f.recipe.id = :recipeId " +
            "AND f.periodStart >= :from AND f.periodEnd <= :to")
    Double calculateAvgSatisfaction(@Param("recipeId") Long recipeId,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    @Query("SELECT AVG(f.emotionScore) FROM ProductFeedback f WHERE f.recipe.id = :recipeId " +
            "AND f.periodStart >= :from AND f.periodEnd <= :to AND f.emotionScore IS NOT NULL")
    Double calculateAvgEmotion(@Param("recipeId") Long recipeId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);

    @Query("SELECT COUNT(f) FROM ProductFeedback f WHERE f.recipe.id = :recipeId " +
            "AND f.periodStart >= :from AND f.periodEnd <= :to")
    Long countByRecipeIdAndPeriod(@Param("recipeId") Long recipeId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);
}
