package com.recipe.manager.repository;

import com.recipe.manager.entity.StoreMonthlyFoodCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreMonthlyFoodCostRepository extends JpaRepository<StoreMonthlyFoodCost, Long> {

    Optional<StoreMonthlyFoodCost> findByStoreIdAndTargetMonth(Long storeId, String targetMonth);

    @Query("SELECT fc FROM StoreMonthlyFoodCost fc WHERE fc.store.id = :storeId " +
            "AND fc.targetMonth BETWEEN :startMonth AND :endMonth " +
            "ORDER BY fc.targetMonth")
    List<StoreMonthlyFoodCost> findByStoreIdAndMonthRange(
            @Param("storeId") Long storeId,
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth);

    @Query("SELECT fc FROM StoreMonthlyFoodCost fc WHERE fc.targetMonth = :targetMonth " +
            "ORDER BY fc.store.name")
    List<StoreMonthlyFoodCost> findByTargetMonth(@Param("targetMonth") String targetMonth);
}
