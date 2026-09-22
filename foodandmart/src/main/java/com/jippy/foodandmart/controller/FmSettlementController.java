package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.FmSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FM Settlement APIs", description = "Merchant settlement APIs in Food and Mart microservice")
public class FmSettlementController {

    private final FmSettlementService fmSettlementService;

    /**
     * Fetches settlement details for all outlets
     * belonging to the specified merchant.
     * <p>
     * Flow:
     * <p>
     * 1. Receive merchantId.
     * <p>
     * 2. Fetch all outlets belonging to the merchant
     * from FM database.
     * <p>
     * 3. For every outlet, fetch settlement calculation
     * from CO microservice using outletId.
     * <p>
     * 4. CO calculates:
     * - merchantTotalPrice
     * - promotionDeductedAmount
     * <p>
     * 5. FM calculates:
     * - GST deducted amount
     * - net settlement amount
     * <p>
     * 6. Return settlement details for all outlets.
     *
     * @param merchantId merchant ID
     * @return list of settlement details for merchant outlets
     */
    @Operation(summary = "Get settlements for merchant", description = "Fetches settlement details for all outlets belonging to the specified merchant and calculates GST, merchant promotion deduction and final settlement amount.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merchant settlement details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid merchant ID"), @ApiResponse(responseCode = "404", description = "No outlets found for the merchant"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/getMerchantSettlementsForOutlets")
        // --NOT USED FOR SETTLEMENTS PRESENTLY
    public ResponseEntity<List<FmMerchantSettlementResponseDto>> getSettlementsForMerchant(

            @Parameter(description = "Merchant ID for which settlement details are required", example = "3", required = true) @RequestParam("merchantId") @NotNull(message = "Merchant ID is required") Integer merchantId) {

        log.info("Received request to get settlements for merchantId: {}", merchantId);

        List<FmMerchantSettlementResponseDto> response = fmSettlementService.getMerchantSettlementsForOutlets(merchantId);

        log.info("Successfully fetched {} settlement records for merchantId: {}", response.size(), merchantId);

        return ResponseEntity.ok(response);
    }
//    ===================================================================================
//    ===================================================================================

    /**
     * Fetches merchant settlement details for the given outlets
     * within the specified date range.
     * <p>
     * Flow:
     * 1. Receive outlet IDs, start date and end date.
     * 2. Fetch merchant details from FM database.
     * 3. Fetch order settlement details from CO microservice.
     * 4. Calculate GST deduction.
     * 5. Calculate final settlement amount.
     *
     * @param requestDto settlement request containing outlet IDs and date range
     * @return merchant settlement details
     */
    @PostMapping("/getMerchantSettlementBetweenDates")
    @Operation(summary = "Get merchant settlement between dates", description = "API-2 FOR SETTLEMENTS" +
            " Fetches merchant settlement details for the given outlets between the selected start date and end date.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merchant settlement details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid settlement request"), @ApiResponse(responseCode = "404", description = "Settlement details not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<List<FmMerchantSettlementBetweenDatesResponseDto>> getMerchantSettlementBetweenDates(@RequestBody FmMerchantSettlementBetweenDatesRequestDto requestDto) {

        log.info("Received request for merchant settlement. " + "Start Date: {}, End Date: {}, Week Slot Days ID: {}", requestDto.getStartDate(), requestDto.getEndDate(), requestDto.getWeekSlotDaysId());

        List<FmMerchantSettlementBetweenDatesResponseDto> response
                = fmSettlementService.getMerchantSettlementBetweenDates(requestDto);

        log.info("Returning {} merchant settlement records", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Fetches outlet and order settlement details
     * for the selected settlement period.
     * <p>
     * Flow:
     * <p>
     * 1. Receive start date and end date.
     * <p>
     * 2. FM calls CO microservice.
     * <p>
     * 3. CO calculates:
     * - Number of unique outlets
     * - Number of delivered orders
     * - Total merchant amount
     * - Promotion deducted amount
     * <p>
     * 4. FM receives the CO calculation.
     * <p>
     * 5. FM calculates:
     * - GST applicability
     * - GST deduction
     * - Final settlement amount
     *
     * @paramm startDate settlement period start date
     * @paramm endDate   settlement period end date
     * @return settlement details including GST and final settlement amount
     */
    @Operation(
            summary = "Get outlet and order settlement details",
            description = "API-1 FOR SETTLEMENTS"+
                    " Fetches delivered order settlement details from CO and calculates GST and final settlement amount in FM. Settlement period can be provided using startDate and endDate or weekSlotDaysId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settlement details fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid settlement request"),
            @ApiResponse(responseCode = "404", description = "Settlement week slot not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/getOutletAndOrdersCountSettlementDetailsBetweenDates")
    public ResponseEntity<FmOutletAndOrdersCountForSettlementResponseDto>
    getOutletAndOrdersSettlementDetails(
            @RequestBody FmMerchantSettlementBetweenDatesRequestDto requestDto) {

        log.info(
                "Received request for outlet and order settlement details. " +
                        "Start Date: {}, End Date: {}, Week Slot ID: {}",
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getWeekSlotDaysId()
        );

        FmOutletAndOrdersCountForSettlementResponseDto response =
                fmSettlementService.getOutletAndOrdersSettlementDetails(requestDto);

        log.info("Successfully calculated outlet and order settlement details.");

        return ResponseEntity.ok(response);
    }

//    ======================================================================================
//    ======================================================================================
@Operation(
        summary = "Get merchant settlement for outlet between dates",
        description = """
                 API-3 FOR SETTLEMENTS
                Fetches settlement details for a specific outlet.

                The settlement period can be provided using either:
                1. startDate and endDate
                2. weekSlotDaysId

                startDate and endDate must not be provided together
                with weekSlotDaysId.
                """
)
@PostMapping("/getMerchantSettlementForOutletBetweenDates")
public ResponseEntity<FmMerchantSettlementForOutletResponseDto>
getMerchantSettlementForOutletBetweenDates(
        @RequestBody FmMerchantSettlementForOutletRequestDto requestDto) {

    log.info(
            "Request received for outlet settlement. outletId: {}, startDate: {}, endDate: {}, weekSlotDaysId: {}",
            requestDto.getOutletId(),
            requestDto.getStartDate(),
            requestDto.getEndDate(),
            requestDto.getWeekSlotDaysId()
    );

    FmMerchantSettlementForOutletResponseDto response =
            fmSettlementService
                    .getMerchantSettlementForOutletBetweenDates(requestDto);

    if (response == null) {

        log.info(
                "No settlement data found for outletId: {}",
                requestDto.getOutletId()
        );

        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(response);
}
//================================================================================
//================================================================================
    /**
     * Fetches merchant settlement order-item details for a specific outlet
     * within the requested settlement period.
     *
     * FM receives order-item details from CO and enriches them with:
     * - Product name
     * - Variant name
     * - GST applicability
     *
     * FM also calculates:
     * - Amount after promotion
     * - GST deduction at fixed 5%
     * - Final settlement amount
     */
    @Operation(
            summary = "Get merchant settlement order details for outlet",
            description = """
                API-4 FOR SETTLEMENTS
                Fetches order-item level merchant settlement details
                for a specific outlet between the selected dates.

                The settlement period can be provided using either:
                1. startDate and endDate
                2. weekSlotDaysId

                CO provides the order and merchant amount details.
                FM enriches the response with product, variant and GST details.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant settlement order details fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid settlement request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Settlement details not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping("/getMerchantSettlementForOutletOrderDetails")
    public ResponseEntity<FmMerchantSettlementForOutletOrderDetailsResponseDto>
    getMerchantSettlementForOutletOrderDetails(
            @RequestBody FmMerchantSettlementForOutletBetweenDatesRequestDto requestDto) {

        log.info(
                "Request received for merchant settlement order details. " +
                        "outletId: {}, startDate: {}, endDate: {}, weekSlotDaysId: {}",
                requestDto.getOutletId(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getWeekSlotDaysId()
        );

        FmMerchantSettlementForOutletOrderDetailsResponseDto response =
                fmSettlementService.getMerchantSettlementForOutletOrderDetails(
                        requestDto
                );

        if (response == null) {

            log.info(
                    "No merchant settlement order details found for outletId: {}",
                    requestDto.getOutletId()
            );

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}