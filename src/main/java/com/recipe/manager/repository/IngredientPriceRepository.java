package com.recipe.manager.repository;

import com.recipe.manager.entity.IngredientPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientPriceRepository extends JpaRepository<IngredientPrice, Long> {

    List<IngredientPrice> findByIngredientIdOrderByEffectiveFromDesc(Long ingredientId);

    @Query("SELECT p FROM IngredientPrice p WHERE p.ingredient.id = :ingredientId " +
           "AND p.effectiveFrom <= :date " +
           "AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date) " +
           "ORDER BY p.effectiveFrom DESC")
    Optional<IngredientPrice> findCurrentPrice(
            @Param("ingredientId") Long ingredientId,
            @Param("date") LocalDate date);
}
