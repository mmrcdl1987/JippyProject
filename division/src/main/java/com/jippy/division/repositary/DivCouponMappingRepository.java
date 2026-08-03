package com.jippy.division.repositary;

import com.jippy.division.entity.DivCouponMappingOutletProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DivCouponMappingRepository
        extends JpaRepository<DivCouponMappingOutletProduct, Integer> {

    /**
     * Active coupon outlets
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
     * Check campaign already exists
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
}