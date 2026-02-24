package com.recipe.manager.repository;

import com.recipe.manager.entity.RecipeCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeCostRepository extends JpaRepository<RecipeCost, Long> {

    Optional<RecipeCost> findByRecipeId(Long recipeId);
}
