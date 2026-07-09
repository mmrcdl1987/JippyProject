package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OutletSubscriptionPlanResponseDto {

    private Integer outletSubscriptionPlanId;
    private Integer outletId;
    private Integer subscriptionPlanId;

    private LocalDate subscriptionFromDate;
    private LocalDate subscriptionToDate;

    private Integer bannerSlotDaysId;


    private LocalDate bannerFromDate;

    private LocalDate bannerToDate;

    private Integer[] mealTypeTimingsIds;


    private List<MealTypeTimingResponseDto> mealTypeTimings;

    private String priceModelType;
    private BigDecimal offerAmount;
}