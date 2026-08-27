package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FmMerchantPriceUpdateRequest {

    @NotNull(message = "Merchant price is required")
    @DecimalMin(
            value = "0.00",
            message = "Merchant price cannot be negative"
    )
    private BigDecimal merchantPrice;

    /**
     * Role coming from UI/login information.
     *
     * Supported:
     * ROLE_MERCHANT
     * ROLE_SUPERADMIN
     * ROLE_DEVADMIN
     */
    @NotNull(message = "Role is required")
    private String role;

    /**
     * Logged-in user id.
     */
    private Integer updatedBy;
}