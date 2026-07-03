package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ActiveBannerProjection {

    Integer getAreaId();

    Integer getOutletId();

    String getOutletName();

    Integer getOutletSubscriptionPlanId();

    Integer getSubscriptionPlanId();

    Integer getBannerSlot();

    Integer getBestRestaurantSlot();

    Integer getDealsSlot();

    String getMainBannerUrl();

    String getBestRestaurantBannerUrl();

    String getDealsBannerUrl();

    LocalDate getBannerFromDate();

    LocalDate getBannerToDate();

    String getPriceModelType();

    BigDecimal getOfferAmount();
}