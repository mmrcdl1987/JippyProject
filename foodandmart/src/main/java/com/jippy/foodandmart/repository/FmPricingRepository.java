package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FmPricingRepository
        extends JpaRepository<FmProductOnlinePricing, Integer> {


    // =========================================================
    // EXISTING ROW CHECK
    // =========================================================

    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND (
                    (:productVariantId IS NULL
                        AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
            """,
            nativeQuery = true)
    int existsRow(
            @Param("productId")
            Integer productId,

            @Param("outletCategoryId")
            Integer outletCategoryId,

            @Param("productVariantId")
            Integer productVariantId
    );


    // =========================================================
    // EXISTING PRICE UPDATE
    // =========================================================

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
                    (:productVariantId IS NULL
                        AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
            """,
            nativeQuery = true)
    int updatePrice(
            @Param("productId")
            Integer productId,

            @Param("outletCategoryId")
            Integer outletCategoryId,

            @Param("productVariantId")
            Integer productVariantId,

            @Param("price")
            BigDecimal price,

            @Param("updatedBy")
            Integer updatedBy,

            @Param("approvedBy")
            Integer approvedBy
    );


    // =========================================================
    // LATEST APPROVED PRICE
    // =========================================================

    Optional<FmProductOnlinePricing>
    findTopByProductIdAndIsApprovedOrderByCreatedAtDesc(
            Integer productId,
            Boolean isApproved
    );


    // =========================================================
    // PRODUCT + OUTLET
    // =========================================================

    @Query("""
            SELECT pop
            FROM FmProductOnlinePricing pop
            JOIN FmOutletCategory oc
                ON oc.outletCategoryId = pop.outletCategoryId
            WHERE pop.productId = :productId
              AND oc.outletId = :outletId
              AND oc.isActive = 'Y'
            """)
    Optional<FmProductOnlinePricing>
    findByProductIdAndOutletId(
            @Param("productId")
            Integer productId,

            @Param("outletId")
            Integer outletId
    );


    // =========================================================
    // ONLINE PRICE BY PRODUCT + CATEGORY
    // =========================================================

    @Query(value = """
            SELECT online_price
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND is_approved = true
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<BigDecimal>
    findOnlinePriceByProductIdAndOutletCategoryId(
            @Param("productId")
            Integer productId,

            @Param("outletCategoryId")
            Integer outletCategoryId
    );


    // =========================================================
    // GET OUTLET CATEGORY
    // =========================================================

    @Query("""
            SELECT oc.outletCategoryId
            FROM FmOutletCategory oc
            JOIN FmProduct p
                ON p.outletCategoryId =
                   oc.outletCategoryId
            WHERE p.productId = :productId
              AND oc.outletId = :outletId
              AND oc.isActive = 'Y'
            """)
    Optional<Integer>
    findOutletCategoryIdByProductAndOutlet(
            @Param("productId")
            Integer productId,

            @Param("outletId")
            Integer outletId
    );


    // =========================================================
    // BULK UPDATE
    //
    // EXISTING METHOD
    //
    // [0] product_online_pricing_id
    // [1] product_id
    // [2] outlet_category_id
    // [3] product_variant_id
    // [4] online_price
    //
    // NO OTHER METHOD CHANGED.
    // =========================================================

    @Query(value = """
            SELECT
                product_online_pricing_id,
                product_id,
                outlet_category_id,
                product_variant_id,
                online_price
            FROM jippy_fm.product_online_pricing
            WHERE outlet_category_id IN (:outletCategoryIds)
            """,
            nativeQuery = true)
    List<Object[]> findExistingBulkPricing(
            @Param("outletCategoryIds")
            List<Integer> outletCategoryIds
    );


    // =========================================================
    // CURRENT PRICE FOR SCHEDULED UPDATE
    // =========================================================

    @Query(value = """
            SELECT online_price
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND (
                    (:productVariantId IS NULL
                        AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
              AND is_approved = true
            ORDER BY updated_at DESC NULLS LAST,
                     product_online_pricing_id DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<BigDecimal>
    findCurrentPriceForScheduledUpdate(
            @Param("productId")
            Integer productId,

            @Param("outletCategoryId")
            Integer outletCategoryId,

            @Param("productVariantId")
            Integer productVariantId
    );


    // =========================================================
    // OUTLET CATEGORIES FOR SCHEDULED PRICES
    // =========================================================

    @Query(value = """
            SELECT
                p.product_id,
                oc.outlet_id,
                oc.outlet_category_id
            FROM jippy_fm.products p
            JOIN jippy_fm.outlet_categories oc
                ON oc.outlet_category_id =
                   p.outlet_category_id
            WHERE p.product_id IN (:productIds)
              AND oc.outlet_id IN (:outletIds)
              AND oc.is_active = 'Y'
            """,
            nativeQuery = true)
    List<Object[]>
    findOutletCategoriesForScheduledPrices(
            @Param("productIds")
            List<Integer> productIds,

            @Param("outletIds")
            List<Integer> outletIds
    );


    // =========================================================
    // CURRENT PRICES FOR SCHEDULED UPDATES
    // =========================================================

    @Query(value = """
            SELECT
                product_id,
                outlet_category_id,
                product_variant_id,
                online_price
            FROM jippy_fm.product_online_pricing
            WHERE outlet_category_id IN (:outletCategoryIds)
              AND is_approved = true
            """,
            nativeQuery = true)
    List<Object[]>
    findCurrentPricesForScheduledUpdates(
            @Param("outletCategoryIds")
            List<Integer> outletCategoryIds
    );


    // =========================================================
    // FIND PRICING RECORD
    // =========================================================

    @Query(value = """
            SELECT *
            FROM jippy_fm.product_online_pricing
            WHERE product_id = :productId
              AND outlet_category_id = :outletCategoryId
              AND (
                    (:productVariantId IS NULL
                        AND product_variant_id IS NULL)
                    OR product_variant_id = :productVariantId
                  )
            ORDER BY updated_at DESC NULLS LAST,
                     product_online_pricing_id DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<FmProductOnlinePricing> findPricingRecord(
            @Param("productId")
            Integer productId,

            @Param("outletCategoryId")
            Integer outletCategoryId,

            @Param("productVariantId")
            Integer productVariantId
    );

    // BULK CURRENT ONLINE PRICES
    @Query(value = """
        SELECT
            pop.product_id,
            pop.product_variant_id,
            pop.online_price,
            p.product_name,
            p.image_link
        FROM jippy_fm.product_online_pricing pop
        JOIN jippy_fm.products p
            ON p.product_id = pop.product_id
        JOIN jippy_fm.outlet_categories oc
            ON oc.outlet_category_id = pop.outlet_category_id
        WHERE oc.outlet_id = :outletId
          AND pop.product_id IN (:productIds)
          AND pop.is_approved = true
          AND oc.is_active = 'Y'
        """,
            nativeQuery = true)
    List<Object[]> findCurrentOnlinePrices(
            @Param("outletId") Integer outletId,
            @Param("productIds") List<Integer> productIds
    );

}