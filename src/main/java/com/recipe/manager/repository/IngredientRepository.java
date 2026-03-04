package com.recipe.manager.repository;

import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.SupplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

    Page<Ingredient> findByCategory(String category, Pageable pageable);

    Page<Ingredient> findBySupplyStatus(SupplyStatus supplyStatus, Pageable pageable);

    Page<Ingredient> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT i FROM Ingredient i WHERE " +
            "(:category IS NULL OR i.category = :category) AND " +
            "(:supplyStatus IS NULL OR i.supplyStatus = :supplyStatus) AND " +
            "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Ingredient> findByFilters(
            @Param("category") String category,
            @Param("supplyStatus") SupplyStatus supplyStatus,
            @Param("keyword") String keyword,
            Pageable pageable);
}
