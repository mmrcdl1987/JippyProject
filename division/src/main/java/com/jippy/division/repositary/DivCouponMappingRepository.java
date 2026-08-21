package com.jippy.division.repositary;

import com.jippy.division.entity.DivCouponMappingOutletProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DivCouponMappingRepository
        extends JpaRepository<DivCouponMappingOutletProduct, Integer> {

    /**
     * Active coupon outlets.
     */
    @Query(value = """
            SELECT DISTINCT cmop.outlet_id
            FROM jippy_division.coupon_mapping_outlets_products cmop
            JOIN jippy_division.promotion_date pd
              ON cmop.promotion_date_id = pd.promotion_date_id
            WHERE CURRENT_DATE BETWEEN
                  CAST(pd.promotion_from_date AS DATE)
              AND CAST(pd.promotion_to_date AS DATE)
            """, nativeQuery = true)
    List<Integer> findActiveCouponOutlets();

    /**
     * Check coupon campaign overlap by location and meal slot.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_division.coupon_mapping_outlets_products cm
            JOIN jippy_division.promotion_date pd
              ON cm.promotion_date_id = pd.promotion_date_id
            WHERE cm.location_id = :locationId
              AND cm.location_type = :locationType
              AND pd.meal_type_slot_id = :mealTypeSlotId
              AND (
                    CAST(pd.promotion_from_date AS DATE) <= :toDate
                AND CAST(pd.promotion_to_date AS DATE) >= :fromDate
              )
            """, nativeQuery = true)
    Integer countCouponCampaign(
            @Param("locationId") Integer locationId,
            @Param("locationType") String locationType,
            @Param("mealTypeSlotId") Integer mealTypeSlotId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /**
     * Find coupon campaign mapping by ID.
     */
    Optional<DivCouponMappingOutletProduct> findByCouponMappingId(
            Integer couponMappingId);

    /**
     * Find all coupon mappings belonging to a promotion date.
     *
     * Required for multi-meal-slot update/delete processing.
     */
    List<DivCouponMappingOutletProduct> findByPromotionDateId(
            Integer promotionDateId);

    /**
     * Delete coupon mapping by mapping ID.
     */
    void deleteByCouponMappingId(
            Integer couponMappingId);

    /**
     * Delete all coupon mappings belonging to a promotion date.
     *
     * Required when rebuilding a campaign with multiple meal slots.
     */
    void deleteByPromotionDateId(
            Integer promotionDateId);
    /**
     * Check coupon product overlap.
     */
    @Query("""
        SELECT COUNT(c)
        FROM DivCouponMappingOutletProduct c
        JOIN DivPromotionDate pd
             ON pd.promotionDateId = c.promotionDateId
        WHERE c.outletId = :outletId
          AND c.productId = :productId
          AND pd.mealTypeSlotId = :mealTypeSlotId
          AND pd.promotionFromDate <= :endDate
          AND pd.promotionToDate >= :startDate
        """)
    Long countCouponOverlap(
            @Param("outletId") Integer outletId,
            @Param("productId") Integer productId,
            @Param("mealTypeSlotId") Integer mealTypeSlotId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    /**
     * Check coupon product overlap during multi-meal campaign update.
     *
     * All promotion_date records belonging to the same campaign
     * share the same createdAt value.
     *
     * Therefore the complete existing campaign is excluded
     * during overlap validation.
     */
    @Query("""
    SELECT COUNT(c)
    FROM DivCouponMappingOutletProduct c
    JOIN DivPromotionDate pd
         ON pd.promotionDateId = c.promotionDateId
    WHERE c.outletId = :outletId
      AND c.productId = :productId
      AND pd.mealTypeSlotId = :mealTypeSlotId
      AND pd.promotionFromDate <= :endDate
      AND pd.promotionToDate >= :startDate
      AND pd.createdAt <> :campaignCreatedAt
    """)
    Long countCouponOverlapForUpdate(
            @Param("outletId") Integer outletId,
            @Param("productId") Integer productId,
            @Param("mealTypeSlotId") Integer mealTypeSlotId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("campaignCreatedAt") LocalDateTime campaignCreatedAt);

    @Query("""
        SELECT DISTINCT c.promotionDateId
        FROM DivCouponMappingOutletProduct c
        WHERE c.couponId = :couponId
        """)
    List<Integer> findPromotionDateIdsByCouponId(
            @Param("couponId") Integer couponId);
}