package com.recipe.manager.repository;

import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByStatusNot(RecipeStatus status, Pageable pageable);

    Page<Recipe> findByStatus(RecipeStatus status, Pageable pageable);

    Page<Recipe> findByStatusNotAndCategory(RecipeStatus status, String category, Pageable pageable);

    Optional<Recipe> findByIdAndStatusNot(Long id, RecipeStatus status);
}
