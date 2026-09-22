package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Main response DTO for merchant settlement order details
 * for a specific outlet and settlement period.
 *
 * The response contains:
 *
 * - Outlet ID
 * - Effective settlement start date
 * - Effective settlement end date
 * - Product/order-level settlement details
 *
 * One order can contain multiple products.
 * Therefore, the same orderId can appear multiple times
 * in the orders list.
 */
@Getter
@Setter
public class CoMerchantSettlementForOutletOrderDetailsResponseDto {

    /**
     * Outlet ID for which settlement details were fetched.
     */
    @Schema(
            description = "Outlet ID for which settlement details were fetched.",
            example = "3"
    )
    private Integer outletId;

    /**
     * Effective settlement start date.
     */
    @Schema(
            description = "Effective settlement start date.",
            example = "2026-09-15"
    )
    private LocalDate startDate;

    /**
     * Effective settlement end date.
     */
    @Schema(
            description = "Effective settlement end date.",
            example = "2026-09-21"
    )
    private LocalDate endDate;

    /**
     * List of order-item level settlement details.
     *
     * One order may contain multiple products,
     * so one orderId can appear in multiple records.
     */
    @Schema(
            description = "List of order-item level merchant settlement details."
    )
    private List<CoMerchantSettlementOrderItemResponseDto> orders;
}