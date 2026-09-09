package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeliveryChargeSettingsDTO {

    private Integer customerDeliveryChargeSettingsId;

    @NotNull(message = "City ID is required")
    private Integer cityId;

    @NotBlank(message = "Plan name is required")
    @Size(max = 30, message = "Plan name must not exceed 30 characters")
    private String planName;

    @NotNull(message = "Order value threshold is required")
    @DecimalMin(value = "0.00", message = "Order value threshold cannot be negative")
    private BigDecimal orderValueThreshold;

    @NotNull(message = "Free distance is required")
    @DecimalMin(value = "0.00", message = "Free distance cannot be negative")
    private BigDecimal freeDistanceKms;

    @NotNull(message = "Charge per KM is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Charge per KM must be greater than zero")
    private BigDecimal chargePerKm;

    private Boolean isActive;
}