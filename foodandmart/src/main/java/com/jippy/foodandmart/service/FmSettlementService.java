package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;

import java.util.List;

public interface FmSettlementService {

    List<FmMerchantSettlementResponseDto> getMerchantSettlementsForOutlets(
            Integer merchantId
    );
    /**
     * Fetches merchant settlement details for the given outlets
     * within the specified date range.
     *
     * @param requestDto settlement request containing outlet IDs and date range
     * @return merchant settlement details
     */
    List<FmMerchantSettlementBetweenDatesResponseDto> getMerchantSettlementBetweenDates(
            FmMerchantSettlementBetweenDatesRequestDto requestDto
    );

    /**
     * Fetches outlet and order settlement details
     * for the selected settlement period.
     *
     * FM calls CO to get:
     * - Number of unique outlets
     * - Number of delivered orders
     * - Total merchant amount
     * - Promotion deducted amount
     *
     * FM then calculates:
     * - GST applicability
     * - GST deduction
     * - Final settlement amount
     *
     * @paramm startDate settlement period start date
     * @paramm endDate settlement period end date
     * @return settlement details including GST and final settlement amount
     */
    FmOutletAndOrdersCountForSettlementResponseDto getOutletAndOrdersSettlementDetails(
            FmMerchantSettlementBetweenDatesRequestDto requestDto
    );

    /**
     * Fetches merchant settlement details for a specific outlet
     * within the requested settlement period.
     *
     * The settlement period can be provided using either:
     *
     * 1. startDate + endDate
     * OR
     * 2. weekSlotDaysId
     *
     * @param requestDto outlet ID and settlement period details
     * @return settlement details for the requested outlet
     */
    FmMerchantSettlementForOutletResponseDto
    getMerchantSettlementForOutletBetweenDates(
            FmMerchantSettlementForOutletRequestDto requestDto
    );
    /**
     * Fetches merchant settlement order details for a specific outlet
     * within the requested settlement period.
     *
     * The CO service provides the order-item information, while FM
     * enriches each order item with:
     *
     * 1. Product name
     * 2. Variant name
     * 3. GST applicability
     *
     * FM then calculates:
     *
     * 1. Amount after merchant promotion
     * 2. GST amount at fixed 5%
     * 3. Final settlement amount after GST
     *
     * One response record is created for each order item.
     *
     * @param requestDto settlement request containing outlet ID
     *                   and settlement date range
     *
     * @return outlet-level settlement response containing
     *         individual order-item settlement details
     */
    FmMerchantSettlementForOutletOrderDetailsResponseDto
    getMerchantSettlementForOutletOrderDetails(
            FmMerchantSettlementForOutletBetweenDatesRequestDto requestDto
    );
}
