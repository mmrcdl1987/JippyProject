package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OutletSubscriptionResponseDto {

    private Integer outletSubscriptionPlanId;
    private Integer outletId;

    private Integer subscriptionPlanId;
    private String planName;

    private LocalDate subscriptionFromDate;
    private LocalDate subscriptionToDate;

    private Long remainingDays;
    private Boolean active;

    private String bannerSlots;
    private Integer bannerDays;
    private String bestRestaurantSlot;
    private String whatsappBroadcast;
    private String videoCredits;
}