package com.recipe.manager.repository;

import com.recipe.manager.entity.IngredientSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientSeasonRepository extends JpaRepository<IngredientSeason, Long> {

    List<IngredientSeason> findByIngredientIdOrderByMonth(Long ingredientId);

    List<IngredientSeason> findByMonth(int month);

    void deleteByIngredientId(Long ingredientId);
}
