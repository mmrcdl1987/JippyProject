package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.iservice.CoSettlementService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/co/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CO Settlement APIs", description = "Settlement calculation APIs in Customer and Order microservice")
public class CoSettlementController {

    private final CoSettlementService coSettlementService;

    /**
     * Calculates settlement-related amounts for an outlet.
     * <p>
     * This API is called by the FM microservice.
     * <p>
     * CO is responsible for:
     * 1. Finding orders for the outlet.
     * 2. Calculating total merchant item price.
     * 3. Calculating merchant promotion discount.
     * <p>
     * GST calculation is handled by FM because
     * isGstApplied belongs to the FM outlet table.
     *
     * @param outletId outlet ID
     * @return merchant total price and promotion deduction
     */
    @Operation(summary = "Calculate settlement for outlet", description = "Calculates merchant total price and merchant promotion discount for the specified outlet.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Settlement calculation completed successfully"), @ApiResponse(responseCode = "400", description = "Invalid outlet ID"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/calculateSettlementForOutlet")
    public ResponseEntity<CoSettlementCalculationDto> calculateSettlementForOutlet(

            @Parameter(description = "Outlet ID", example = "3", required = true) @RequestParam @NotNull(message = "Outlet ID is required") Integer outletId) {

        log.info("Received settlement calculation request for outletId: {}", outletId);

        CoSettlementCalculationDto response = coSettlementService.calculateSettlementForOutlet(outletId);

        log.info("Settlement calculation completed for outletId: {}", outletId);

        return ResponseEntity.ok(response);
    }

    //    =========================================================================================
//    =========================================================================================
    @PostMapping("/getMerchantSettlement")
    public ResponseEntity<List<CoMerchantSettlementSummaryDto>> getMerchantSettlement(@RequestBody CoMerchantSettlementBetweenDatesRequestDto requestDto) {

        return ResponseEntity.ok(coSettlementService.getMerchantSettlement(requestDto));
    }

//==========================================================================================
//==========================================================================================

    /**
     * Fetches outlet and order settlement details
     * for the selected settlement period.
     * <p>
     * CO calculates only the order-related settlement values:
     * <p>
     * 1. Number of unique outlets having delivered orders.
     * 2. Number of delivered orders.
     * 3. Total merchant amount.
     * 4. Total merchant promotion deduction.
     * <p>
     * GST calculation and final settlement calculation
     * are handled by FM.
     *
     * @param startDate settlement period start date
     * @param endDate   settlement period end date
     * @return settlement details for the selected period
     */
    @Operation(summary = "Get outlet and order settlement details", description = "Fetches delivered order and merchant settlement details and counts between the selected start date and end date.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Settlement details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid settlement dates"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/getOutletAndOrdersSettlementDetails")
    public ResponseEntity<CoOutletAndOrdersSettlementResponseDto> getOutletAndOrdersSettlementDetails(

            @Parameter(description = "Settlement period start date", example = "2026-09-01", required = true) @RequestParam("startDate") LocalDate startDate,

            @Parameter(description = "Settlement period end date", example = "2026-09-07", required = true) @RequestParam("endDate") LocalDate endDate) {

        log.info("Received request for outlet and order settlement details. Start Date: {}, End Date: {}", startDate, endDate);

        CoOutletAndOrdersSettlementResponseDto response = coSettlementService.getOutletAndOrdersSettlementDetails(startDate, endDate);

        log.info("Successfully fetched outlet and order settlement details. Start Date: {}, End Date: {}", startDate, endDate);

        return ResponseEntity.ok(response);
    }

//    =====================================================================================
//    =====================================================================================

    /**
     * Fetches merchant settlement details for a specific outlet
     * within the requested settlement period.
     * <p>
     * Only ORDER_DELIVERED orders are considered for settlement.
     * <p>
     * CO receives the outlet ID, start date and end date
     * from the FM microservice.
     */
    @Operation(summary = "Get merchant settlement for outlet between dates", description = """
            Fetches delivered order count, merchant total amount
            and merchant promotion deduction for a specific outlet
            within the settlement period.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet settlement details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid settlement request"), @ApiResponse(responseCode = "404", description = "No settlement data found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping("/getMerchantSettlementForOutletBetweenDates")
    public ResponseEntity<CoMerchantSettlementSummaryDto> getMerchantSettlementForOutletBetweenDates(@RequestBody CoMerchantSettlementForOutletRequestDto requestDto) {

        log.info("Request received for outlet settlement. " + "outletId: {}, startDate: {}, endDate: {}", requestDto.getOutletId(), requestDto.getStartDate(), requestDto.getEndDate());

        CoMerchantSettlementSummaryDto response = coSettlementService.getMerchantSettlementForOutlet(requestDto);

        /*
         * No delivered orders were found for this outlet
         * during the requested settlement period.
         */
        if (response == null) {

            log.info("No settlement data found for outletId: {}", requestDto.getOutletId());

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
//    ======================================================================================
//    ======================================================================================

    /**
     * Fetches delivered order-item details required for
     * merchant settlement for a specific outlet and date range.
     * <p>
     * Only orders with status ORDER_DELIVERED are included.
     * <p>
     * FM-specific information such as product name, variant name,
     * GST and final settlement calculation will be handled by FM.
     *
     * @param outletId  outlet ID for which settlement details are required
     * @param startDate settlement start date/time
     * @param endDate   settlement end date/time
     * @return list of CO order-item settlement details
     */
    @Operation(summary = "Get merchant settlement order details", description = "Fetches delivered order-item settlement details for a specific outlet and date range.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merchant settlement order details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid settlement request"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/orderDetailsForOutlet")
    public ResponseEntity<List<CoMerchantSettlementOrderItemDto>>
    getMerchantSettlementOrderDetails(
            @RequestParam Integer outletId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        List<CoMerchantSettlementOrderItemDto> response =
                coSettlementService.getMerchantSettlementOrderDetails(
                        outletId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(response);
    }
}
