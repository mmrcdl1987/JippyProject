package com.jippy.division.repositary;

import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DivPriceDropMappingRepository extends JpaRepository<DivPriceDropMappingOutletsProduct, Integer> {

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
     * Check Price Drop Campaign Already Exists
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_division.price_drop_mapping_outlets_products pm
            
            JOIN jippy_division.promotion_date pd
              ON pm.promotion_date_id = pd.promotion_date_id
            
            WHERE pm.location_id = :locationId
              AND pm.location_type = :locationType
              AND pd.meal_type_slot_id = :mealTypeSlotId
            
              AND (
                    CAST(pd.promotion_from_date AS DATE) <= :toDate
                AND CAST(pd.promotion_to_date AS DATE) >= :fromDate
              )
            """, nativeQuery = true)
    Integer countPriceDropCampaign(@Param("locationId") Integer locationId, @Param("locationType") String locationType, @Param("mealTypeSlotId") Integer mealTypeSlotId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    Optional<DivPriceDropMappingOutletsProduct> findByPriceDropMappingOutletsProductsId(Integer priceDropMappingOutletsProductsId);

    void deleteByPriceDropMappingOutletsProductsId(Integer priceDropMappingOutletsProductsId);

    @Query(value = """
            SELECT COUNT(*)
            FROM jippy_division.price_drop_mapping_outlets_products pm
            
            JOIN jippy_division.promotion_date pd
              ON pm.promotion_date_id = pd.promotion_date_id
            
            WHERE pm.price_drop_mapping_outlets_products_id <> :campaignId
              AND pm.location_id = :locationId
              AND pm.location_type = :locationType
              AND pd.meal_type_slot_id = :mealTypeSlotId
            
              AND (
                    CAST(pd.promotion_from_date AS DATE) <= :toDate
                AND CAST(pd.promotion_to_date AS DATE) >= :fromDate
              )
            """, nativeQuery = true)
    Integer countPriceDropCampaignForUpdate(@Param("campaignId") Integer campaignId, @Param("locationId") Integer locationId, @Param("locationType") String locationType, @Param("mealTypeSlotId") Integer mealTypeSlotId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query("""
            SELECT COUNT(p)
            FROM DivPriceDropMappingOutletsProduct p
            JOIN DivPromotionDate pd
                 ON pd.promotionDateId = p.promotionDateId
            WHERE p.outletId = :outletId
              AND p.productId = :productId
              AND pd.promotionFromDate <= :endDate
              AND pd.promotionToDate >= :startDate
            """)
    Long countPriceDropOverlap(@Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COUNT(p)
            FROM DivPriceDropMappingOutletsProduct p
            JOIN DivPromotionDate pd
            ON pd.promotionDateId = p.promotionDateId
            WHERE p.priceDropMappingOutletsProductsId <> :campaignId
            AND p.outletId = :outletId
            AND p.productId = :productId
            AND pd.promotionFromDate <= :endDate
            AND pd.promotionToDate >= :startDate
            """)
    Long countPriceDropOverlapForUpdate(@Param("campaignId") Integer campaignId, @Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}