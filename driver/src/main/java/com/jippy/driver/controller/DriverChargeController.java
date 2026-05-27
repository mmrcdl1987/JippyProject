package com.jippy.driver.controller;

import com.jippy.driver.dto.DeliveryChargeCalculationRequestDto;
import com.jippy.driver.dto.DeliveryChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverChargeCalculationRequestDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;
import com.jippy.driver.service.DriverChargeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@Slf4j
public class DriverChargeController {

    private final DriverChargeService driverChargeService;

    // DRIVER PAYOUT CALCULATION
    @PostMapping("/driver-charge/calculate")
    @Operation(summary = "Calculate Driver Charge", description = "Calculate pickup and delivery charge for driver payout")
    public ResponseEntity<DriverChargeCalculationResponseDto> calculateDriverCharge(@Valid @RequestBody DriverChargeCalculationRequestDto requestDto) {

        log.info("API_START | CALCULATE_DRIVER_CHARGE");

        DriverChargeCalculationResponseDto response = driverChargeService.calculateDriverCharge(requestDto);

        log.info("API_SUCCESS | DRIVER_CHARGE_CALCULATED");

        return ResponseEntity.ok(response);
    }

    // CHECKOUT DELIVERY CHARGE
    @PostMapping("/delivery-charge/calculate")
    @Operation(summary = "Calculate Delivery Charge", description = "Calculate delivery charge during customer checkout")
    public ResponseEntity<DeliveryChargeCalculationResponseDto> calculateDeliveryCharge(@Valid @RequestBody DeliveryChargeCalculationRequestDto requestDto) {

        log.info("API_START | CALCULATE_DELIVERY_CHARGE");

        DeliveryChargeCalculationResponseDto response = driverChargeService.calculateDeliveryCharge(requestDto);

        log.info("API_SUCCESS | DELIVERY_CHARGE_CALCULATED");

        return ResponseEntity.ok(response);
    }
}