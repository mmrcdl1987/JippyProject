package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmSubscriptionPlanDto {

    private Integer subscriptionPlanId;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100)
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1)
    private Integer durationInDays;

    @NotNull(message = "Radius is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal radiusInKms;

    @Size(max = 50)
    private String bannerSlots;

    private Integer bannerDays;

    @Size(max = 50)
    private String bestRestaurantSlot;

    @Size(max = 50)
    private String whatsappBroadcast;

    @Size(max = 50)
    private String videoCredits;

    @NotNull(message = "State id is required")
    private Integer stateId;

    @NotNull(message = "City id is required")
    private Integer cityId;

    @NotNull(message = "Area id is required")
    private Integer areaId;
}