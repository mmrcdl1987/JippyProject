package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FmPricingRepository extends JpaRepository<FmProductOnlinePricing, Integer> {

    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND (
                    (:productVariantId IS NULL AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
            """, nativeQuery = true)
    int existsRow(@Param("productId") Integer productId, @Param("outletCategoryId") Integer outletCategoryId, @Param("productVariantId") Integer productVariantId);

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
              AND (
                    (:productVariantId IS NULL AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
            """, nativeQuery = true)
    int updatePrice(@Param("productId") Integer productId, @Param("outletCategoryId") Integer outletCategoryId, @Param("productVariantId") Integer productVariantId, @Param("price") BigDecimal price, @Param("updatedBy") Integer updatedBy, @Param("approvedBy") Integer approvedBy);

    Optional<FmProductOnlinePricing> findTopByProductIdAndIsApprovedOrderByCreatedAtDesc(Integer productId, Boolean isApproved);


    @Query("""
            SELECT pop
            FROM FmProductOnlinePricing pop
            JOIN FmOutletCategory oc
                ON oc.outletCategoryId = pop.outletCategoryId
            WHERE pop.productId = :productId
              AND oc.outletId = :outletId
              AND oc.isActive = 'Y'
            """)
    Optional<FmProductOnlinePricing> findByProductIdAndOutletId(@Param("productId") Integer productId, @Param("outletId") Integer outletId);

    @Query(value = """
            SELECT online_price
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND is_approved = true
            LIMIT 1
            """, nativeQuery = true)
    Optional<BigDecimal> findOnlinePriceByProductIdAndOutletCategoryId(@Param("productId") Integer productId, @Param("outletCategoryId") Integer outletCategoryId);

    @Query("""
            SELECT oc.outletCategoryId
            FROM FmOutletCategory oc
            JOIN FmProduct p
                ON p.outletCategoryId = oc.outletCategoryId
            WHERE p.productId = :productId
              AND oc.outletId = :outletId
              AND oc.isActive = 'Y'
            """)
    Optional<Integer> findOutletCategoryIdByProductAndOutlet(@Param("productId") Integer productId, @Param("outletId") Integer outletId);


    @Query(value = """
            SELECT
                product_online_pricing_id,
                product_id,
                outlet_category_id,
                product_variant_id,
                online_price
            FROM jippy_fm.product_online_pricing
            WHERE outlet_category_id IN (:outletCategoryIds)
            """, nativeQuery = true)
    List<Object[]> findExistingBulkPricing(@Param("outletCategoryIds") List<Integer> outletCategoryIds);

    /**
     * Find current online price for a specific product,
     * outlet category and variant.
     */
    @Query(value = """
            SELECT online_price
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND (
                    (:productVariantId IS NULL AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
              AND is_approved = true
            ORDER BY updated_at DESC NULLS LAST,
                     product_online_pricing_id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<BigDecimal> findCurrentPriceForScheduledUpdate(@Param("productId") Integer productId, @Param("outletCategoryId") Integer outletCategoryId, @Param("productVariantId") Integer productVariantId);


    @Query(value = """
            SELECT
                p.product_id,
                oc.outlet_id,
                oc.outlet_category_id
            FROM jippy_fm.products p
            JOIN jippy_fm.outlet_categories oc
                ON oc.outlet_category_id = p.outlet_category_id
            WHERE p.product_id IN (:productIds)
              AND oc.outlet_id IN (:outletIds)
              AND oc.is_active = 'Y'
            """, nativeQuery = true)
    List<Object[]> findOutletCategoriesForScheduledPrices(@Param("productIds") List<Integer> productIds, @Param("outletIds") List<Integer> outletIds);

    @Query(value = """
            SELECT
                product_id,
                outlet_category_id,
                product_variant_id,
                online_price
            FROM jippy_fm.product_online_pricing
            WHERE outlet_category_id IN (:outletCategoryIds)
              AND is_approved = true
            """, nativeQuery = true)
    List<Object[]> findCurrentPricesForScheduledUpdates(@Param("outletCategoryIds") List<Integer> outletCategoryIds);


}