package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.PromotionPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}