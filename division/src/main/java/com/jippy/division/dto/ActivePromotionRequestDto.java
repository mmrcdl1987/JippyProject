package com.jippy.division.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ActivePromotionRequestDto {

    @NotNull(message = "Customer Id is required")
    private Integer customerId;

    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    /**
     * Optional.
     *
     * If productIds are provided:
     * - Return product promotions for these products
     * - Return general coupons
     * - Return product-specific coupons
     *
     * If productIds are null or empty:
     * - Return only outlet/customer-level applicable coupons
     */
    private List<Integer> productIds;
}