package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ActiveBannerResponseDto {

    private Integer areaId;

    private Integer outletId;

    private String outletName;

    private Integer outletSubscriptionPlanId;

    private Integer subscriptionPlanId;

    private Integer bannerSlot;

    private Integer bestRestaurantSlot;

    private Integer dealsSlot;

    private Integer bannerSlotDaysId;

    private LocalDate bannerFromDate;

    private LocalDate bannerToDate;

    private List<MealTypeTimingResponseDto> mealTypeTimings;

    private String mainBannerUrl;

    private String bestRestaurantBannerUrl;

    private String dealsBannerUrl;

    private String priceModelType;

    private BigDecimal offerAmount;

    private BigDecimal radiusInKms;

    private Double latitude;

    private Double longitude;

    private Integer[] mealTypeTimingsIds;
}