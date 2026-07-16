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

    @Query(value = " SELECT " +
    "sp.area_id                        AS areaId,"+
    "o.outlet_id                       AS outletId,"+
    "o.outlet_name                     AS outletName,"+
    "osp.outlet_subscription_plan_id     AS outletSubscriptionPlanId,"+
   "osp.subscription_plan_id           AS subscriptionPlanId,"+
   "sp.banner_slot                    AS bannerSlot,"+
    "sp.best_restaurant_slot            AS bestRestaurantSlot,"+
    "sp.deals_slot                     AS dealsSlot,"+
    "osp.main_banner_url                AS mainBannerUrl,"+
    "osp.best_restaurant_banner_url      AS bestRestaurantBannerUrl,"+
    "osp.deals_banner_url               AS dealsBannerUrl,"+
    "bsd.slot_start_date                AS bannerFromDate,"+
    "bsd.slot_end_date                  AS bannerToDate,"+
   " osp.banner_slot_days_id           AS bannerSlotDaysId,"+
   " osp.meal_type_timings_ids           AS mealTypeTimingsIds,"+
   " osp.price_model_type               AS priceModelType,"+
    "osp.offer_amount                  AS offerAmount, "+
            "sp.radius_in_kms           AS radiusInKms, "+
            "ST_X(o.outlet_location::geometry) as longitude, ST_Y(o.outlet_location::geometry) as latitude "+
   " FROM jippy_fm.outlet_subscription_plans osp " +
    "JOIN jippy_fm.subscription_plans sp "+
    "ON sp.subscription_plan_id = osp.subscription_plan_id "+
    "JOIN jippy_fm.outlets o "+
    "ON o.outlet_id = osp.outlet_id "+
    "JOIN jippy_fm.week_slot_days bsd "+
    "ON bsd.week_slot_days_id = osp.banner_slot_days_id "+
    "WHERE CURRENT_DATE "+
    "BETWEEN bsd.slot_start_date "+
    "AND bsd.slot_end_date "+
    "AND o.is_active = 'Y' "+
    "AND o.outlet_location IS NOT NULL "+
    "AND o.is_approved = true "+
   " ORDER BY sp.banner_slot DESC ", nativeQuery = true)
    List<ActiveBannerProjection> findActiveBanners();

    long countByBannerSlotDaysId(Integer bannerSlotDaysId);

    Optional<FmOutletSubscriptionPlan> findBySubscriptionPlanId(Integer subscriptionPlanId);

    @Query(value = "SELECT EXISTS (" +
            "  SELECT 1 FROM jippy_fm.outlet_subscription_plans " +
            "  WHERE banner_slot_days_id = :bannerSlotDaysId " +
            "    AND subscription_plan_id = :subscriptionPlanId " +
            "    AND meal_type_timings_ids  && :mealTypeTimingsIds  "+
            ")", nativeQuery = true)
    boolean findByBannerSlotDaysSubscriptionPlansAndMealTypes(
           @Param("bannerSlotDaysId") Integer bannerSlotDaysId,
            @Param("subscriptionPlanId") Integer subscriptionPlanId,
            @Param("mealTypeTimingsIds") Integer[] mealTypeTimingsIds);

}