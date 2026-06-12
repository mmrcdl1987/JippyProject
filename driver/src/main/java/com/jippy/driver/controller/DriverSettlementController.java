package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverIncentiveSettlementResponseDto;
import com.jippy.driver.dto.DriverSettlementResponseDto;
import com.jippy.driver.service.DriverSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/driver")
public class DriverSettlementController {

    private final DriverSettlementService driverSettlementService;

    // this fetches settlement details for all drivers between the specified start and end dates.
// The response includes driver ID, name, total settlements,
// total amount, and settlement details for each driver.
    @GetMapping("/getDriversSettlements")
    @Operation(summary = "Get Driver Settlements", description = "Fetches driver settlement " + "details between the specified start date and end date. format as (yyyy-MM-dd) " + "[ api path ]-> /api/driver/getDriversSettlements?startDate=2026-06-01&endDate=2026-06-30")
    public ResponseEntity<List<DriverSettlementResponseDto>> getDriversSettlements(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        log.info("Received request to fetch driver settlements | startDate={}, endDate={}", startDate, endDate);
        log.info("Validating date range: startDate={}, endDate={}", startDate, endDate);

        List<DriverSettlementResponseDto> response = driverSettlementService.getDriversSettlements(startDate, endDate);

        log.info("Successfully fetched settlements for {} drivers", response.size());

        return ResponseEntity.ok(response);
    }

//    this fetches previous month incentives for all drivers.
//    The response includes driver ID, total incentives amount, and incentive details
//    for each driver.
//    api path -> /api/driver/getDriversIncentivesForSettlements?filter=currentMonth
    @GetMapping("/getDriversIncentivesForSettlements")
    @Operation(summary = "Get Drivers Incentives For Settlements",
            description = "Fetches total incentives amount for each driver based on the provided filter. "
                    +"filter can be 'currentMonth' to fetch incentives for the previous month. "
                   + "[ api path ]-> /api/driver/getDriversIncentivesForSettlements?filter=currentMonth" +
                    "(fetches previous month start to end )")
    public ResponseEntity<List<DriverIncentiveSettlementResponseDto>> getDriversIncentivesForSettlements(
            @Parameter(description = "currentMonth")
            @RequestParam String filter) {

        log.info("Received request to fetch driver incentive settlements for filter : {}", filter);
        log.info("Validating filter: -> currentMonth(i.e PreviousMonth) {}", filter);

        List<DriverIncentiveSettlementResponseDto> response =
                driverSettlementService.getDriversIncentivesForSettlements(filter);

        log.info("Successfully fetched incentive settlements for {} drivers", response.size());
        return ResponseEntity.ok(response);
    }
}