package com.jippy.division.repositary;

import com.jippy.division.entity.PromotionSchedule;
import com.jippy.division.enums.PromotionSourceType;
import com.jippy.division.projection.DivActiveDiscountsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionScheduleRepository
        extends JpaRepository<PromotionSchedule, Long> {

    void deleteBySourceTypeAndSourceId(
            PromotionSourceType sourceType,
            Integer sourceId);



@Query(value = """
        SELECT
            ps.outlet_id AS "outletId",
            ps.product_id AS "productId",
            ps.source_type AS "sourceType",
        
            -- Min/Max Schedule Timings
            MIN(ps.start_date_time) AS "startDateTime",
            MAX(ps.end_date_time) AS "endDateTime",
        
            -- AGGREGATE ALL SLOTS INTO A SINGLE COMMA-SEPARATED STRING ("2,3")
            STRING_AGG(DISTINCT CAST(pd.meal_type_slot_id AS text), ',') AS "mealTypeSlotIdsStr",
        
            c.coupon_code AS "couponCode",
            c.usage_limit_per_user AS "usageLimitPerUser",
            COALESCE(c.min_order_value, 0.00) AS "minOrderValue",
            COALESCE(pdm.price_drop_value, c.discount_value, 0.00) AS "discountAmount",
            COALESCE(c.price_model_id, pdm.price_model_id) AS "priceModelId",
            pml.price_model_name AS "priceModelName",
            ps.promotion_schedule_id as "promotionScheduleId",
            ps.source_id as "sourceId"
        
        FROM jippy_division.promotion_schedules ps
        
        LEFT JOIN jippy_division.coupon_mapping_outlets_products cm
            ON cm.coupon_mapping_id = ps.source_id AND ps.source_type = 'COUPON'
        
        LEFT JOIN jippy_division.coupons c
            ON cm.coupon_id = c.coupon_id
        
        LEFT JOIN jippy_division.price_drop_mapping_outlets_products pdm
            ON ps.source_type = 'PRICE_DROP'
           AND ps.source_id = pdm.price_drop_mapping_outlets_products_id
        
        LEFT JOIN jippy_division.price_model pml
            ON pml.price_model_id = COALESCE(c.price_model_id, pdm.price_model_id)
        
        LEFT JOIN jippy_division.promotion_date pd
            ON pd.promotion_date_id = COALESCE(cm.promotion_date_id, pdm.promotion_date_id)
        
        WHERE ps.source_type IN ('COUPON', 'PRICE_DROP')
          AND :now BETWEEN ps.start_date_time AND ps.end_date_time
        
        -- GROUP BY THE BASE COUPON/PROMOTION (Removing source_id and schedule_id from GROUP BY)
        GROUP BY
            ps.outlet_id,
            ps.product_id,
            ps.source_type,
            c.coupon_id,
            c.coupon_code,
            c.usage_limit_per_user,
            c.min_order_value,
            pdm.price_drop_value,
            c.discount_value,
            c.price_model_id,
            pdm.price_model_id,
            pml.price_model_name,
            ps.source_id,
            ps.promotion_schedule_id -- Added ps.promotion_schedule_id
        ORDER BY "startDateTime" DESC """
       ,nativeQuery = true)
    List<DivActiveDiscountsProjection> getActiveDiscounts(@Param("now") LocalDateTime now);
}