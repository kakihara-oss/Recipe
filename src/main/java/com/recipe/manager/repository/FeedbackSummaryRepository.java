package com.recipe.manager.repository;

import com.recipe.manager.entity.FeedbackSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackSummaryRepository extends JpaRepository<FeedbackSummary, Long> {

    Page<FeedbackSummary> findByRecipeIdOrderByPeriodStartDesc(Long recipeId, Pageable pageable);

    Optional<FeedbackSummary> findByRecipeIdAndPeriodStartAndPeriodEnd(
            Long recipeId, java.time.LocalDate periodStart, java.time.LocalDate periodEnd);

    List<FeedbackSummary> findByRecipeIdOrderByPeriodStartAsc(Long recipeId);
}
