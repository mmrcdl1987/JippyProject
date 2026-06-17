package com.jippy.foodandmart.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionPlanResponseDto {

    private Integer subscriptionPlanId;
    private String planName;
    private BigDecimal price;
    private Integer durationInDays;
    private BigDecimal radiusInKms;
    private String bannerSlots;
    private Integer bannerDays;
    private String bestRestaurantSlot;
    private String whatsappBroadcast;
    private String videoCredits;
}