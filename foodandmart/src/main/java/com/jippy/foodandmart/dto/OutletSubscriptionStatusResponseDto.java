package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OutletSubscriptionStatusResponseDto {

    private Integer outletId;

    private String outletName;

    private Integer subscriptionPlanId;

    private String planName;

    private String subscriptionStatus;

    private LocalDate subscriptionFromDate;

    private LocalDate subscriptionToDate;

    private LocalDate bannerFromDate;

    private LocalDate bannerToDate;

    private Integer bannerSlot;

    private Integer bestRestaurantSlot;

    private Integer dealsSlot;

    private String mainBannerUrl;

    private String bestRestaurantBannerUrl;

    private String dealsBannerUrl;

    private String priceModelType;
}