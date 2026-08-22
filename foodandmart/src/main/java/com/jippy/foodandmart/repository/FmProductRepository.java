package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.projections.FmMasterProductCategoryProjection;
import com.jippy.foodandmart.projections.FmProductCategoryProjection;
import com.jippy.foodandmart.projections.FmProductPriceProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmProductRepository
        extends JpaRepository<FmProduct, Integer> {

    // ============================================================
    // BASIC PRODUCT METHODS
    // ============================================================

    List<FmProduct> findByOutletCategoryId(
            Integer outletCategoryId
    );


    Optional<FmProduct> findByOutletCategoryIdAndProductNameIgnoreCase(
            Integer outletCategoryId,
            String productName
    );


    boolean existsByOutletCategoryIdAndProductNameIgnoreCase(
            Integer outletCategoryId,
            String productName
    );


    long countByOutletCategoryId(
            Integer outletCategoryId
    );


    Optional<FmProduct> findById(
            Integer productId
    );


    boolean existsByProductId(
            Integer productId
    );


    // ============================================================
    // APPROVED FLOW - WITH PRICING
    // ============================================================

    @Query(value = """
            SELECT
                p.product_id,
                p.product_name,
                p.merchant_price,
                pop.online_price
            FROM jippy_fm.products p
            JOIN jippy_fm.outlet_categories oc
                ON p.outlet_category_id = oc.outlet_category_id
            LEFT JOIN jippy_fm.product_online_pricing pop
                ON pop.product_id = p.product_id
               AND pop.outlet_category_id = oc.outlet_category_id
            WHERE oc.outlet_id IN (:outletIds)
            """, nativeQuery = true)
    List<Object[]> findProducts(
            @Param("outletIds") List<Integer> outletIds
    );


    // ============================================================
    // UNAPPROVED FLOW - WITHOUT PRICING
    // ============================================================

    @Query(value = """
            SELECT
                p.product_id,
                p.product_name,
                p.merchant_price,
                NULL AS online_price
            FROM jippy_fm.products p
            JOIN jippy_fm.outlet_categories oc
                ON p.outlet_category_id = oc.outlet_category_id
            WHERE oc.outlet_id IN (:outletIds)
            """, nativeQuery = true)
    List<Object[]> findProductsWithoutPricing(
            @Param("outletIds") List<Integer> outletIds
    );


    // ============================================================
    // PRODUCT / OUTLET CATEGORY
    // ============================================================

    @Query(value = """
            SELECT
                p.outlet_category_id
            FROM jippy_fm.products p
            WHERE p.product_id = :productId
            """, nativeQuery = true)
    Integer findOutletCategoryId(
            @Param("productId") Integer productId
    );


    @Query("""
            SELECT p
            FROM FmProduct p
            JOIN p.outletCategory oc
            WHERE oc.outletId = :outletId
              AND p.outletCategoryId IN :categoryIds
            """)
    List<FmProduct> findByOutletIdAndOutletCategoryIds(
            @Param("outletId") Integer outletId,
            @Param("categoryIds") List<Integer> categoryIds
    );


    @Query("""
            SELECT p
            FROM FmProduct p
            WHERE p.outletCategoryId IN :categoryIds
            """)
    List<FmProduct> findByOutletCategoryIds(
            @Param("categoryIds") List<Integer> categoryIds
    );


    // ============================================================
    // PRODUCT TOGGLE
    // ============================================================

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmProduct p
            SET p.isToggle = false
            WHERE p.productId = :productId
            """)
    void disableProduct(
            @Param("productId") Integer productId
    );


    @Modifying
    @Transactional
    @Query("""
            UPDATE FmProduct p
            SET p.isToggle = true
            WHERE p.productId = :productId
            """)
    void enableProduct(
            @Param("productId") Integer productId
    );


    // ============================================================
    // PERMANENTLY CLOSE PRODUCT
    // ============================================================

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmProduct p
            SET p.isActive = :status,
                p.isToggle = false
            WHERE p.productId = :productId
            """)
    void permanentlyCloseProduct(
            @Param("productId") Integer productId,
            @Param("status") String status
    );


    Optional<FmProduct> findByProductIdAndIsActive(
            Integer productId,
            String isActive
    );


    // ============================================================
    // PRODUCT + OUTLET VALIDATION
    // ============================================================

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END
            FROM FmProduct p
            JOIN FmOutletCategory oc
                ON p.outletCategoryId = oc.outletCategoryId
            WHERE p.productId = :productId
              AND oc.outletId = :outletId
            """)
    boolean existsByProductIdAndOutletId(
            @Param("productId") Integer productId,
            @Param("outletId") Integer outletId
    );


    @Query("""
            SELECT COUNT(p) > 0
            FROM FmProduct p
            JOIN p.outletCategory oc
            WHERE oc.outletId = :outletId
              AND p.productId = :productId
              AND p.isActive = 'Y'
              AND p.isToggle = true
            """)
    boolean existsProductInOutlet(
            @Param("outletId") Integer outletId,
            @Param("productId") Integer productId
    );


    // ============================================================
    // ACTIVE PRODUCTS
    // ============================================================

    @Query("""
            SELECT p.productId
            FROM FmProduct p
            JOIN p.outletCategory oc
            WHERE oc.outletId = :outletId
              AND p.isActive = 'Y'
              AND p.isToggle = true
            ORDER BY p.productId
            """)
    List<Integer> findActiveProductIdsByOutlet(
            @Param("outletId") Integer outletId
    );


    // ============================================================
    // IMAGE / DESCRIPTION UPDATE
    // ============================================================

    /**
     * Fetch all products whose image and description are not updated.
     */
    Page<FmProduct> findByIsImageDescUpdatedFalse(
            Pageable pageable
    );


    // ============================================================
    // PRODUCT PRICE PROJECTION - SINGLE OUTLET
    // ============================================================

    @Query(value = """
            SELECT
                p.product_id AS productId,
                p.product_name AS productName,
                NULL AS variantId,
                NULL AS variantName,
                p.merchant_price AS merchantPrice,
                pop.online_price AS onlinePrice
            FROM jippy_fm.outlets o
            INNER JOIN jippy_fm.outlet_categories oc
                ON o.outlet_id = oc.outlet_id
            INNER JOIN jippy_fm.product_online_pricing pop
                ON oc.outlet_category_id = pop.outlet_category_id
            INNER JOIN jippy_fm.products p
                ON pop.product_id = p.product_id
            WHERE o.outlet_id = :outletId
            ORDER BY p.product_name
            """, nativeQuery = true)
    List<FmProductPriceProjection> findProductsByOutletId(
            @Param("outletId") Integer outletId
    );


    // ============================================================
    // BULK PRICING
    // ============================================================

    /**
     * Fetch all products for multiple outlets in a single query.
     *
     * Returns:
     *
     * [0] productId
     * [1] productName
     * [2] merchantPrice
     * [3] outletCategoryId
     * [4] outletId
     */
    @Query(value = """
            SELECT
                p.product_id,
                p.product_name,
                p.merchant_price,
                p.outlet_category_id,
                oc.outlet_id
            FROM jippy_fm.products p
            INNER JOIN jippy_fm.outlet_categories oc
                ON oc.outlet_category_id = p.outlet_category_id
            WHERE oc.outlet_id IN (:outletIds)
              AND p.is_active = 'Y'
              AND oc.is_active = 'Y'
            ORDER BY oc.outlet_id, p.product_id
            """, nativeQuery = true)
    List<Object[]> findProductsForBulkPricing(
            @Param("outletIds") List<Integer> outletIds
    );


    // ============================================================
    // ACTIVE PRODUCT + VARIANT VALIDATION
    // ============================================================

    @Query(value = """
            SELECT
                EXISTS (
                    SELECT 1
                    FROM jippy_fm.products p
                    INNER JOIN jippy_fm.outlet_categories oc
                        ON oc.outlet_category_id = p.outlet_category_id
                    WHERE p.product_id = :productId
                      AND oc.outlet_id = :outletId
                      AND p.is_active = 'Y'
                      AND oc.is_active = 'Y'
                )
                AND (
                    :variantId IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jippy_fm.product_variant_options pvo
                        WHERE pvo.product_id = :productId
                          AND pvo.product_variant_options_id = :variantId
                          AND pvo.is_active = true
                    )
                )
            """, nativeQuery = true)
    boolean existsActiveProductAndVariantInOutlet(
            @Param("productId") Integer productId,
            @Param("outletId") Integer outletId,
            @Param("variantId") Integer variantId
    );


    // ============================================================
    // PRODUCT CATEGORY DETAILS
    // ============================================================

    /**
     * Products
     *      ↓
     * outlet_categories
     *      ↓
     * outlets
     *      ↓
     * categories
     */
    @Query(value = """
            SELECT
                p.product_id AS productId,
                p.product_name AS productName,
                oc.outlet_category_id AS outletCategoryId,
                oc.outlet_id AS outletId,
                c.category_id AS categoryId,
                c.category_name AS categoryName,
                o.outlet_name AS outletName
            FROM jippy_fm.products p
            LEFT JOIN jippy_fm.outlet_categories oc
                ON p.outlet_category_id = oc.outlet_category_id
            LEFT JOIN jippy_fm.outlets o
                ON oc.outlet_id = o.outlet_id
            LEFT JOIN jippy_fm.categories c
                ON oc.category_id = c.category_id
            WHERE LOWER(p.product_name) = LOWER(:productName)
              AND p.is_active = 'Y'
              AND oc.is_active = 'Y'
              AND o.is_active = 'Y'
            """, nativeQuery = true)
    List<FmProductCategoryProjection> findProductCategoryDetails(
            @Param("productName") String productName
    );


    // ============================================================
    // MASTER PRODUCT CATEGORY DETAILS
    // ============================================================

    /**
     * Fetch master product category information.
     */
    @Query(value = """
            SELECT
                mp.master_product_id AS masterProductId,
                mp.master_product_name AS masterProductName,
                mp.category_id AS categoryId,
                mp.category_name AS categoryName
            FROM jippy_fm.master_products mp
            WHERE LOWER(mp.master_product_name) = LOWER(:productName)
            """, nativeQuery = true)
    List<FmMasterProductCategoryProjection>
    findMasterProductCategoryDetails(
            @Param("productName") String productName
    );


    // ============================================================
    // OUTLET CATEGORY ID BY PRODUCT NAME
    // ============================================================

    @Query(value = """
            SELECT
                p.outlet_category_id
            FROM jippy_fm.products p
            WHERE LOWER(p.product_name) = LOWER(:productName)
              AND p.is_active = 'Y'
            """, nativeQuery = true)
    List<Integer> findOutletCategoryIdsByProductName(
            @Param("productName") String productName
    );


    // ============================================================
    // UPDATE OUTLET CATEGORY
    // ============================================================

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE jippy_fm.outlet_categories
            SET category_id = :updatedCategoryId,
                updated_at = CURRENT_TIMESTAMP
            WHERE outlet_category_id = :outletCategoryId
              AND is_active = 'Y'
            """, nativeQuery = true)
    int updateOutletCategoryId(
            @Param("outletCategoryId") Integer outletCategoryId,
            @Param("updatedCategoryId") Integer updatedCategoryId
    );


    // ============================================================
    // UPDATE MASTER PRODUCT CATEGORY
    // ============================================================

    /**
     * All records having the same master_product_name
     * will be updated.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE jippy_fm.master_products
            SET category_id = :updatedCategoryId,
                updated_at = CURRENT_TIMESTAMP
            WHERE LOWER(master_product_name) = LOWER(:productName)
            """, nativeQuery = true)
    int updateMasterProductCategoryId(
            @Param("productName") String productName,
            @Param("updatedCategoryId") Integer updatedCategoryId
    );


    // ============================================================
    // CATEGORY VALIDATION
    // ============================================================

    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_fm.categories
            WHERE category_id = :categoryId
            """, nativeQuery = true)
    long countCategoryById(
            @Param("categoryId") Integer categoryId
    );


    // ============================================================
    // FETCH OUTLET ID FOR PRODUCT
    // ============================================================

    @Query(value = """
            SELECT oc.outlet_id
            FROM jippy_fm.products p
            JOIN jippy_fm.outlet_categories oc
                ON oc.outlet_category_id = p.outlet_category_id
            WHERE p.product_id = :productId
            LIMIT 1
            """, nativeQuery = true)
    Integer fetchOutletIdForProductId(
            @Param("productId") Integer productId
    );
}