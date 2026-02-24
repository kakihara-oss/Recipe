package com.recipe.manager.repository;

import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.SupplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

    Page<Ingredient> findByCategory(String category, Pageable pageable);

    Page<Ingredient> findBySupplyStatus(SupplyStatus supplyStatus, Pageable pageable);

    Page<Ingredient> findByCategoryAndSupplyStatus(String category, SupplyStatus supplyStatus, Pageable pageable);

    @Query("SELECT DISTINCT ri.recipe.id FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    List<Long> findRecipeIdsByIngredientId(@Param("ingredientId") Long ingredientId);
}
