package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCartUpdateRequestDto {

    @NotNull(message = "Customer id is required")
    @Min(value = 1, message = "Customer id must be greater than zero")
    private Integer customerId;

    @NotNull(message = "Outlet id is required")
    @Min(value = 1, message = "Outlet id must be greater than zero")
    private Integer outletId;

    @NotNull(message = "Product id is required")
    @Min(value = 1, message = "Product id must be greater than zero")
    private Integer productId;

    /**
     * Nullable because normal products may not have a variant.
     *
     * For variant products, this represents
     * product_variant_options_id.
     */
    @Min(value = 1, message = "Variant option id must be greater than zero")
    private Integer variantOptionId;

    /**
     * Quantity = 0 means remove the exact
     * product + variant from cart.
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    /**
     * UI sends the selected unit price.
     */
    @NotNull(message = "Unit price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Unit price cannot be negative"
    )
    private BigDecimal unitPrice;
}