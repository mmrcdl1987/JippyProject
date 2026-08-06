package com.jippy.division.repositary;

import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DivPriceDropMappingRepository
        extends JpaRepository<DivPriceDropMappingOutletsProduct, Integer> {

    /**
     * Active Price Drop Outlets
     */
    @Query(value = """
            SELECT DISTINCT pdmop.outlet_id
            FROM jippy_division.price_drop_mapping_outlets_products pdmop
            JOIN jippy_division.promotion_date pd
              ON pdmop.promotion_date_id = pd.promotion_date_id
            WHERE CURRENT_DATE BETWEEN
                  CAST(pd.promotion_from_date AS DATE)
              AND CAST(pd.promotion_to_date AS DATE)
            """, nativeQuery = true)
    List<Integer> findActivePriceDropOutlets();

    /**
     * PHASE 1 & 2: Check if a Price Drop Campaign is running at Location Level (STATE or CITY).
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_division.price_drop_mapping_outlets_products pm
            JOIN jippy_division.promotion_date pd
              ON pm.promotion_date_id = pd.promotion_date_id
            WHERE pm.location_id = :locationId
              AND UPPER(pm.location_type) = UPPER(:locationType)
              AND pd.meal_type_slot_id = :mealTypeSlotId
              AND (
                    CAST(pd.promotion_from_date AS DATE) <= :toDate
                AND CAST(pd.promotion_to_date AS DATE) >= :fromDate
              )
            """, nativeQuery = true)
    Integer countPriceDropCampaignForLocation(
            @Param("locationId") Integer locationId,
            @Param("locationType") String locationType,
            @Param("mealTypeSlotId") Integer mealTypeSlotId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /**
     * PHASE 3: Check if ANY of the selected Outlets already have an active Price Drop Campaign.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_division.price_drop_mapping_outlets_products pm
            JOIN jippy_division.promotion_date pd
              ON pm.promotion_date_id = pd.promotion_date_id
            WHERE pm.outlet_id IN (:outletIds)
              AND pd.meal_type_slot_id = :mealTypeSlotId
              AND (
                    CAST(pd.promotion_from_date AS DATE) <= :toDate
                AND CAST(pd.promotion_to_date AS DATE) >= :fromDate
              )
            """, nativeQuery = true)
    Integer countPriceDropCampaignForOutlets(
            @Param("outletIds") List<Integer> outletIds,
            @Param("mealTypeSlotId") Integer mealTypeSlotId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}