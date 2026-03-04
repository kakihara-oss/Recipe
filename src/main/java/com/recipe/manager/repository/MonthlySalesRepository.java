package com.recipe.manager.repository;

import com.recipe.manager.entity.MonthlySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySalesRepository extends JpaRepository<MonthlySales, Long> {

    List<MonthlySales> findByStoreIdAndTargetMonth(Long storeId, String targetMonth);

    Optional<MonthlySales> findByStoreIdAndRecipeIdAndTargetMonth(
            Long storeId, Long recipeId, String targetMonth);

    List<MonthlySales> findByStoreId(Long storeId);

    @Query("SELECT ms FROM MonthlySales ms WHERE ms.store.id = :storeId " +
            "AND ms.targetMonth BETWEEN :startMonth AND :endMonth " +
            "ORDER BY ms.targetMonth, ms.recipe.title")
    List<MonthlySales> findByStoreIdAndMonthRange(
            @Param("storeId") Long storeId,
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth);

    void deleteByStoreIdAndTargetMonth(Long storeId, String targetMonth);
}
