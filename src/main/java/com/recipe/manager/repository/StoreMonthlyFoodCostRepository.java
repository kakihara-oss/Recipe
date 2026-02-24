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

    Optional<StoreMonthlyFoodCost> findByStoreIdAndSalesMonth(Long storeId, String salesMonth);

    @Query("SELECT fc FROM StoreMonthlyFoodCost fc WHERE fc.salesMonth = :salesMonth " +
           "ORDER BY fc.store.id ASC")
    List<StoreMonthlyFoodCost> findBySalesMonth(@Param("salesMonth") String salesMonth);

    @Query("SELECT fc FROM StoreMonthlyFoodCost fc WHERE fc.store.id = :storeId " +
           "AND fc.salesMonth >= :fromMonth AND fc.salesMonth <= :toMonth " +
           "ORDER BY fc.salesMonth ASC")
    List<StoreMonthlyFoodCost> findByStoreIdAndMonthRange(
            @Param("storeId") Long storeId,
            @Param("fromMonth") String fromMonth,
            @Param("toMonth") String toMonth);
}
