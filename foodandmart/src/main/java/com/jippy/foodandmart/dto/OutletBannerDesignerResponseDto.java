package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OutletBannerDesignerResponseDto {

    private Integer outletId;

    private String outletName;

    private Integer subscriptionPlanId;

    private String planName;

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

    private String whatsappBroadcast;

    private String videoCredits;

    private BigDecimal radiusInKms;

    private String priceModelType;

    private BigDecimal offerAmount;
}