package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.PromotionPlanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionPlanProductRepository extends JpaRepository<PromotionPlanProduct, Integer> {

    List<PromotionPlanProduct> findByPromotionPlanPromotionPlanId(
            Integer promotionPlanId);

    List<PromotionPlanProduct> findByProductId(
            Integer productId);

    List<PromotionPlanProduct> findByOutletCategoryId(
            Integer outletCategoryId);

    void deleteByPromotionPlanPromotionPlanId(
            Integer promotionPlanId);

    /**
     * Create Validation - Product Overlap
     */
    @Query("""
            SELECT COUNT(ppp)
            FROM PromotionPlanProduct ppp
            JOIN ppp.promotionPlan pp
            WHERE pp.outletId = :outletId
              AND ppp.productId IN :productIds
              AND pp.planStartDate <= :endDate
              AND pp.planEndDate >= :startDate
            """)
    Long countOverlappingProductPromotions(
            @Param("outletId") Integer outletId,
            @Param("productIds") List<Integer> productIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Update Validation - Product Overlap
     */
    @Query("""
            SELECT COUNT(ppp)
            FROM PromotionPlanProduct ppp
            JOIN ppp.promotionPlan pp
            WHERE pp.promotionPlanId <> :promotionPlanId
              AND pp.outletId = :outletId
              AND ppp.productId IN :productIds
              AND pp.planStartDate <= :endDate
              AND pp.planEndDate >= :startDate
            """)
    Long countOverlappingProductPromotionsForUpdate(
            @Param("promotionPlanId") Integer promotionPlanId,
            @Param("outletId") Integer outletId,
            @Param("productIds") List<Integer> productIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Create Validation - Category Overlap
     */
    @Query("""
            SELECT COUNT(ppp)
            FROM PromotionPlanProduct ppp
            JOIN ppp.promotionPlan pp
            WHERE pp.outletId = :outletId
              AND ppp.outletCategoryId IN :categoryIds
              AND pp.planStartDate <= :endDate
              AND pp.planEndDate >= :startDate
            """)
    Long countOverlappingCategoryPromotions(
            @Param("outletId") Integer outletId,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Update Validation - Category Overlap
     */
    @Query("""
            SELECT COUNT(ppp)
            FROM PromotionPlanProduct ppp
            JOIN ppp.promotionPlan pp
            WHERE pp.promotionPlanId <> :promotionPlanId
              AND pp.outletId = :outletId
              AND ppp.outletCategoryId IN :categoryIds
              AND pp.planStartDate <= :endDate
              AND pp.planEndDate >= :startDate
            """)
    Long countOverlappingCategoryPromotionsForUpdate(
            @Param("promotionPlanId") Integer promotionPlanId,
            @Param("outletId") Integer outletId,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}