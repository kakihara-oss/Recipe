package com.recipe.manager.repository;

import com.recipe.manager.entity.RecipeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeHistoryRepository extends JpaRepository<RecipeHistory, Long> {

    List<RecipeHistory> findByRecipeIdOrderByChangedAtDesc(Long recipeId);
}
