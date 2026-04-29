package com.jippy.foodandmart.repository;
import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface FmPricingRepository extends JpaRepository<FmProductOnlinePricing, Integer> {

    // GET CURRENT PRICE (LATEST ONLY)
    @Query(value = """
    SELECT online_price
    FROM jippy_fm.product_online_pricing
    WHERE product_id = :productId
      AND outlet_category_id = :outletCategoryId
    ORDER BY updated_at DESC
    LIMIT 1
""", nativeQuery = true)
    Optional<BigDecimal> findCurrentPrice(
            @Param("productId") Integer productId,
            @Param("outletCategoryId") Integer outletCategoryId
    );
    //  CHECK EXISTING ROW
    @Query(value = """
    SELECT COUNT(*)
    FROM jippy_fm.product_online_pricing
    WHERE product_id = :productId
      AND outlet_category_id = :outletCategoryId
""", nativeQuery = true)
    int existsRow(
            @Param("productId") Integer productId,
            @Param("outletCategoryId")Integer outletCategoryId
    );

    // UPDATE EXISTING PRICE
    @Modifying
    @Query(value = """
    UPDATE jippy_fm.product_online_pricing
    SET online_price = :price,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = :updatedBy,
        is_approved = true,
        approved_by = :approvedBy
    WHERE product_id = :productId
      AND outlet_category_id = :outletCategoryId
""", nativeQuery = true)
    int updatePrice(
            @Param("productId") Integer productId,
            @Param("outletCategoryId") Integer outletCategoryId,
            @Param("price") BigDecimal price,
            @Param("updatedBy") Integer updatedBy,
            @Param("approvedBy") Integer approvedBy
    );
}