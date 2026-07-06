package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.projections.ActiveBannerProjection;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
            
                osp.priceModelType               AS priceModelType,
                osp.offerAmount                  AS offerAmount
            
            FROM FmOutletSubscriptionPlan osp
            JOIN FmSubscriptionPlan sp
                 ON sp.subscriptionPlanId = osp.subscriptionPlanId
            JOIN FmOutlet o
                 ON o.outletId = osp.outletId
           
            WHERE o.isActive = 'Y'
            
            ORDER BY
                sp.areaId,
                sp.bannerSlot DESC
            """)
    List<ActiveBannerProjection> findActiveBanners();
    //Removed banner from date and to date from query(in where clause and columns) to resolve compilation issue
}