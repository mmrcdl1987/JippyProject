    package com.jippy.customerandorder.iservice;

    import com.jippy.customerandorder.dto.*;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;

    public interface CoSettlementService {

        /**
         * Calculates settlement amounts for all orders
         * belonging to the supplied outlet.
         */
        CoSettlementCalculationDto calculateSettlementForOutlet(
                Integer outletId);

        /**
         * Fetches merchant settlements for the given outlets
         * between the selected dates or settlement week slot.
         *
         * @param requestDto settlement request
         * @return merchant settlement summary for each outlet
         */
        List<CoMerchantSettlementSummaryDto> getMerchantSettlement(
                CoMerchantSettlementBetweenDatesRequestDto requestDto
        );

        /**
         * Fetches outlet and order settlement details
         * for the selected settlement period.
         *
         * CO is responsible for calculating:
         *
         * 1. Number of unique outlets having delivered orders.
         * 2. Number of delivered orders.
         * 3. Total merchant amount.
         * 4. Total merchant promotion deduction.
         *
         * FM will handle:
         * - GST calculation
         * - GST deduction
         * - Final settlement amount
         *
         * @param startDate settlement period start date
         * @param endDate settlement period end date
         * @return settlement details calculated by CO
         */
        CoOutletAndOrdersSettlementResponseDto getOutletAndOrdersSettlementDetails(
                LocalDate startDate,
                LocalDate endDate
        );

        /**
         * Fetches merchant settlement details for a specific outlet
         * within the requested settlement period.
         *
         * CO receives the already-resolved startDate and endDate
         * from FM.
         *
         * CO calculates:
         * - Delivered order count
         * - Merchant total amount
         * - Merchant promotion deduction
         *
         * GST calculation is handled by FM.
         *
         * @param requestDto outlet ID and settlement date range
         * @return settlement details for the outlet
         */
        CoMerchantSettlementSummaryDto getMerchantSettlementForOutlet(
                CoMerchantSettlementForOutletRequestDto requestDto
        );
        /**
         * Fetches delivered order-item details required for
         * merchant settlement calculation.
         *
         * Data is fetched from CO tables:
         *
         * - orders
         * - order_items
         * - order_price_breakup
         *
         * Only orders matching all of the following conditions
         * are considered:
         *
         * 1. Order belongs to the requested outlet.
         * 2. Order status is ORDER_DELIVERED.
         * 3. Order created_at is within the requested date range.
         *
         * FM-specific details such as product name, variant name,
         * GST applicability, etc. are handled by FM.
         *
         * @param outletId  outlet ID for which settlement is required
         * @param startDate start date/time of settlement period
         * @param endDate   end date/time of settlement period
         *
         * @return list of delivered order-item settlement details
         */
        List<CoMerchantSettlementOrderItemDto> getMerchantSettlementOrderDetails(
                Integer outletId,
                LocalDateTime startDate,
                LocalDateTime endDate
        );
        /**
         * Fetches complete merchant settlement order details for a specific outlet
         * within the requested settlement period.
         *
         * CO is responsible for:
         * - Fetching delivered order items
         * - Fetching merchant total price
         * - Fetching merchant promotion deduction
         * - Fetching productId and variantOptionId
         *
         * FM is responsible for:
         * - Product name
         * - Variant name
         * - GST applicability
         * - GST percentage
         *
         * The final response is returned as product/order-level
         * settlement details.
         *
         * @param requestDto outlet ID and settlement period
         * @return complete order-item settlement details
         */
//        FmMerchantSettlementForOutletOrderDetailsResponseDto
//        getMerchantSettlementForOutletOrderDetails(
//                CoMerchantSettlementForOutletRequestDto requestDto
//        );

    }