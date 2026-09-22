package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Main response DTO for merchant settlement order details
 * for a specific outlet and settlement period.
 *
 * The response contains the effective settlement period
 * and product-level settlement details for every order item.
 */
@Getter
@Setter
public class FmMerchantSettlementForOutletOrderDetailsResponseDto {

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
     *
     * If weekSlotDaysId was provided, this will contain
     * the start date derived from the week slot.
     */
    @Schema(
            description = "Effective settlement start date.",
            example = "2026-09-15"
    )
    private LocalDate startDate;

    /**
     * Effective settlement end date.
     *
     * If weekSlotDaysId was provided, this will contain
     * the end date derived from the week slot.
     */
    @Schema(
            description = "Effective settlement end date.",
            example = "2026-09-21"
    )
    private LocalDate endDate;

    /**
     * Product-level settlement details.
     *
     * One order can contain multiple products,
     * so the same orderId can appear multiple times.
     */
    @Schema(
            description = "List of order item level merchant settlement details."
    )
    private List<FmMerchantSettlementOrderDetailsResponseDto> orders;
}