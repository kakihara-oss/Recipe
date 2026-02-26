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

    List<MonthlySales> findByStoreIdAndSalesMonth(Long storeId, String salesMonth);

    Optional<MonthlySales> findByStoreIdAndRecipeIdAndSalesMonth(Long storeId, Long recipeId, String salesMonth);

    List<MonthlySales> findByStoreIdAndSalesMonthOrderByRecipeId(Long storeId, String salesMonth);

    @Query("SELECT ms FROM MonthlySales ms WHERE ms.store.id = :storeId " +
           "AND ms.salesMonth >= :fromMonth AND ms.salesMonth <= :toMonth " +
           "ORDER BY ms.salesMonth ASC, ms.recipe.id ASC")
    List<MonthlySales> findByStoreIdAndMonthRange(
            @Param("storeId") Long storeId,
            @Param("fromMonth") String fromMonth,
            @Param("toMonth") String toMonth);

    @Query("SELECT ms FROM MonthlySales ms WHERE ms.salesMonth = :salesMonth " +
           "ORDER BY ms.store.id ASC, ms.recipe.id ASC")
    List<MonthlySales> findBySalesMonth(@Param("salesMonth") String salesMonth);

    void deleteByStoreIdAndSalesMonth(Long storeId, String salesMonth);
}
