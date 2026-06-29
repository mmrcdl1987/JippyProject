package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmSubscriptionPlanRequestDto {

    private Integer subscriptionPlanId;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Duration in days is required")
    @Positive(message = "Duration must be greater than zero")
    private Integer durationInDays;

    private Integer bannerDurationInDays;

    private BigDecimal radiusInKms;

    private Integer bannerSlot;

    private Integer bestRestaurantSlot;

    private Integer dealsSlot;

    private String whatsappBroadcast;

    private String videoCredits;

   /* @NotNull(message = "State id is required")
    private Integer stateId;

    @NotNull(message = "City id is required")
    private Integer cityId;*/

    @NotNull(message = "Area id is required")
    private Integer areaId;

    @NotNull(message = "User Id is required")
    private Integer userId;
}