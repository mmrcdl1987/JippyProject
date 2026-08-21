package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.PromotionPlan;
import com.jippy.foodandmart.projections.FmActivePromotionDiscountsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface PromotionPlanRepository extends
        JpaRepository<PromotionPlan, Integer>,
        JpaSpecificationExecutor<PromotionPlan> {

    @EntityGraph(attributePaths = "promotionPlanType")
    Optional<PromotionPlan> findByPromotionPlanId(Integer promotionPlanId);

    Optional<PromotionPlan> findByOutletIdAndOfferNameIgnoreCase(
            Integer outletId,
            String offerName);

    Optional<PromotionPlan> findByOutletIdAndOfferNameIgnoreCaseAndPromotionPlanIdNot(
            Integer outletId,
            String offerName,
            Integer promotionPlanId);

    List<PromotionPlan> findByPromotionPlanTypePromotionPlanTypesId(
            Integer promotionPlanTypesId);

    @Query(
            value = """
                    SELECT *
                    FROM jippy_fm.promotion_plans p
                    WHERE p.outlet_id = :outletId
                    AND (
                        :status = 'ALL'
                        OR (
                            :status = 'ACTIVE'
                            AND CURRENT_TIMESTAMP BETWEEN
                                (p.plan_start_date + p.plan_start_time)
                            AND
                                (p.plan_end_date + p.plan_end_time)
                        )
                        OR (
                            :status = 'SCHEDULED'
                            AND CURRENT_TIMESTAMP <
                                (p.plan_start_date + p.plan_start_time)
                        )
                        OR (
                            :status = 'ENDED'
                            AND CURRENT_TIMESTAMP >
                                (p.plan_end_date + p.plan_end_time)
                        )
                    )
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM jippy_fm.promotion_plans p
                    WHERE p.outlet_id = :outletId
                    AND (
                        :status = 'ALL'
                        OR (
                            :status = 'ACTIVE'
                            AND CURRENT_TIMESTAMP BETWEEN
                                (p.plan_start_date + p.plan_start_time)
                            AND
                                (p.plan_end_date + p.plan_end_time)
                        )
                        OR (
                            :status = 'SCHEDULED'
                            AND CURRENT_TIMESTAMP <
                                (p.plan_start_date + p.plan_start_time)
                        )
                        OR (
                            :status = 'ENDED'
                            AND CURRENT_TIMESTAMP >
                                (p.plan_end_date + p.plan_end_time)
                        )
                    )
                    """,
            nativeQuery = true
    )
    Page<PromotionPlan> findByOutletAndStatus(
            @Param("outletId") Integer outletId,
            @Param("status") String status,
            Pageable pageable);

    @Query(value = """
            SELECT
                pp.promotion_plans_id,
                pp.outlet_id,
                ppt.plan_name AS plan_type,
                pp.offer_name,
                pp.offer_type,
                pp.offer_amount,
                pp.minimum_order_value,
                -- If product_id in plan_products is NULL, fallback to product_id from the category join
                COALESCE(ppp.product_id, p.product_id) AS product_id,
                ppp.outlet_category_id,
                -- 💡 CALCULATE END DATE TIME FOR REDIS TTL:
                    -- If end_time exists: plan_end_date + plan_end_time
                    -- If end_time IS NULL: plan_end_date + 23:59:59
                    CASE
                        WHEN pp.plan_end_time IS NOT NULL
                            THEN (pp.plan_end_date + pp.plan_end_time)
                        ELSE
                            (pp.plan_end_date + TIME '23:59:59')
                    END AS end_date_time
            FROM jippy_fm.promotion_plans pp
            INNER JOIN jippy_fm.promotion_plan_types ppt
                ON pp.promotion_plan_types_id = ppt.promotion_plan_types_id
            LEFT JOIN jippy_fm.promotion_plan_products ppp
                ON pp.promotion_plans_id = ppp.promotion_plans_id
                
            -- Join products table when product_id is NULL to expand category into individual products
            
            LEFT JOIN jippy_fm.products p
                ON p.outlet_category_id = ppp.outlet_category_id
               AND ppp.product_id IS NULL
            WHERE
                CURRENT_DATE BETWEEN pp.plan_start_date AND pp.plan_end_date
                AND (
                    (pp.plan_start_time IS NULL AND pp.plan_end_time IS NULL)
                    OR
                    (:now BETWEEN pp.plan_start_time AND pp.plan_end_time)
                ) AND pp.outlet_id IN (:outletIds) ;""",
    nativeQuery = true)
    List<FmActivePromotionDiscountsProjection> getActivePromtionDiscounts(@Param("now") LocalDateTime now,
            @Param("outletIds") List<Integer> outletIds);


    @Query(value = """
        SELECT COUNT(*)
        FROM jippy_fm.promotion_plans pp
        JOIN jippy_fm.promotion_plan_products ppp
          ON pp.promotion_plans_id = ppp.promotion_plans_id
        WHERE pp.outlet_id = :outletId
          AND ppp.product_id = :productId
          AND (
                pp.plan_start_date + pp.plan_start_time
              ) <= :endDateTime
          AND (
                pp.plan_end_date + pp.plan_end_time
              ) >= :startDateTime
        """, nativeQuery = true)
    long countMerchantPromotionOverlap(
            @Param("outletId") Integer outletId,
            @Param("productId") Integer productId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = """
        SELECT COUNT(*)
        FROM jippy_fm.promotion_plans pp
        JOIN jippy_fm.promotion_plan_products ppp
          ON pp.promotion_plans_id = ppp.promotion_plans_id
        WHERE pp.promotion_plans_id <> :promotionPlanId
          AND pp.outlet_id = :outletId
          AND ppp.product_id = :productId
          AND (
                pp.plan_start_date + pp.plan_start_time
              ) <= :endDateTime
          AND (
                pp.plan_end_date + pp.plan_end_time
              ) >= :startDateTime
        """, nativeQuery = true)
    long countMerchantPromotionOverlapForUpdate(
            @Param("promotionPlanId") Integer promotionPlanId,
            @Param("outletId") Integer outletId,
            @Param("productId") Integer productId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}