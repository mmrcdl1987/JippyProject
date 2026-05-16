package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProduct;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmProductRepository extends JpaRepository<FmProduct, Integer> {
    List<FmProduct> findByOutletCategoryId(Integer outletCategoryId);
    Optional<FmProduct> findByOutletCategoryIdAndProductNameIgnoreCase(Integer outletCategoryId, String productName);
    boolean existsByOutletCategoryIdAndProductNameIgnoreCase(Integer outletCategoryId, String productName);
    long countByOutletCategoryId(Integer outletCategoryId);

    // APPROVED FLOW (WITH PRICING - NEW DESIGN)
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
    List<Object[]> findProducts(@Param("outletIds") List<Integer> outletIds);


    // UNAPPROVED FLOW (NO PRICING)
    @Query(value = """
        SELECT 
            p.product_id,
            p.product_name,
            p.merchant_price,
            NULL as online_price
        FROM jippy_fm.products p
        JOIN jippy_fm.outlet_categories oc 
          ON p.outlet_category_id = oc.outlet_category_id
        WHERE oc.outlet_id IN (:outletIds)
        """, nativeQuery = true)
    List<Object[]> findProductsWithoutPricing(@Param("outletIds") List<Integer> outletIds);


    // REQUIRED FOR SERVICE (STEP 4)
    @Query(value = """
        SELECT outlet_category_id
        FROM jippy_fm.products
        WHERE product_id = :productId
        """, nativeQuery = true)
    Integer findOutletCategoryId(@Param("productId") Integer productId);

    Optional<FmProduct> findById(Integer productId);

    boolean existsByProductId(Integer productId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmProduct p
            SET p.isToggle = false
            WHERE p.productId = :productId
            """)
    void disableProduct(Integer productId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmProduct p
            SET p.isToggle = true
            WHERE p.productId = :productId
            """)
    void enableProduct(Integer productId);

    @Modifying
    @Query("""
           UPDATE FmProduct p
           SET p.isActive = :status,
               p.isToggle = false
           WHERE p.productId = :productId
           """)
    void permanentlyCloseProduct(
            @Param("productId")
            Integer productId,
            @Param("status")
            String status);
}
