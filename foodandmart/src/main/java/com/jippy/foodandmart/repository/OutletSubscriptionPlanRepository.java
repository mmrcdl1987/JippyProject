package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.projections.ActiveBannerProjection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutletSubscriptionPlanRepository
        extends JpaRepository<FmOutletSubscriptionPlan, Integer> {

    Optional<FmOutletSubscriptionPlan> findByOutletId(Integer outletId);
    @Query("""
SELECT
    sp.areaId                        AS areaId,
    o.outletId                       AS outletId,
    o.outletName                     AS outletName,

    osp.outletSubscriptionPlanId     AS outletSubscriptionPlanId,
    osp.subscriptionPlanId           AS subscriptionPlanId,

    sp.bannerSlot                    AS bannerSlot,
    sp.bestRestaurantSlot            AS bestRestaurantSlot,
    sp.dealsSlot                     AS dealsSlot,

    osp.mainBannerUrl                AS mainBannerUrl,
    osp.bestRestaurantBannerUrl      AS bestRestaurantBannerUrl,
    osp.dealsBannerUrl               AS dealsBannerUrl,

    bsd.slotStartDate                AS bannerFromDate,
    bsd.slotEndDate                  AS bannerToDate,

    osp.bannerSlotDaysId             AS bannerSlotDaysId,
    osp.mealTypeTimingsIds           AS mealTypeTimingsIds,

    osp.priceModelType               AS priceModelType,
    osp.offerAmount                  AS offerAmount

FROM FmOutletSubscriptionPlan osp

JOIN FmSubscriptionPlan sp
ON sp.subscriptionPlanId = osp.subscriptionPlanId

JOIN FmOutlet o
ON o.outletId = osp.outletId

JOIN BannerSlotDay bsd
ON bsd.bannerSlotDaysId = osp.bannerSlotDaysId

WHERE CURRENT_DATE
BETWEEN bsd.slotStartDate
AND bsd.slotEndDate

AND o.isActive = 'Y'

ORDER BY
sp.areaId,
sp.bannerSlot DESC
""")
    List<ActiveBannerProjection> findActiveBanners();



    long countByBannerSlotDaysId(Integer bannerSlotDaysId);

    Optional<FmOutletSubscriptionPlan> findBySubscriptionPlanId(Integer subscriptionPlanId);

    @Query(value = "SELECT EXISTS (" +
            "  SELECT 1 FROM jippy_fm.outlet_subscription_plans " +
            "  WHERE banner_slot_days_id = :bannerSlotDaysId " +
            "    AND subscription_plan_id = :subscriptionPlanId " +
            "    AND meal_type_timings_ids  && ARRAY[:mealTypeTimingsIds]  "+
            ")", nativeQuery = true)
    boolean findByBannerSlotDaysSubscriptionPlansAndMealTypes(
           @Param("bannerSlotDaysId") Integer bannerSlotDaysId,
            @Param("subscriptionPlanId") Integer subscriptionPlanId,
            @Param("mealTypeTimingsIds") List mealTypeTimingsIds);

}