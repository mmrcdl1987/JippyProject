package com.jippy.foodandmart.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FmSubscriptionPlanResponseDto {

    private Integer subscriptionPlanId;
    private String planName;
    private BigDecimal price;
    private Integer durationInDays;
    private Integer bannerDurationInDays;
    private BigDecimal radiusInKms;
    private Integer bannerSlot;
    private Integer bestRestaurantSlot;
    private Integer dealsSlot;
    private String whatsappBroadcast;
    private String videoCredits;
    private Integer stateId;
    private String stateName;
    private Integer cityId;
    private String cityName;
    private Integer areaId;
    private String areaName;
}
