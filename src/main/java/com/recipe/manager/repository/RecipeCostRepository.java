package com.recipe.manager.repository;

import com.recipe.manager.entity.RecipeCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeCostRepository extends JpaRepository<RecipeCost, Long> {

    Optional<RecipeCost> findByRecipeId(Long recipeId);

    @Query("SELECT rc FROM RecipeCost rc WHERE rc.targetMarginRate IS NOT NULL " +
            "AND rc.totalIngredientCost IS NOT NULL AND rc.currentPrice IS NOT NULL " +
            "AND (rc.currentPrice - rc.totalIngredientCost) / rc.currentPrice * 100 < rc.targetMarginRate")
    List<RecipeCost> findWarningRecipes();
}
