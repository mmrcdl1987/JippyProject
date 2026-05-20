package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverChargeCalculationRequestDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;
import com.jippy.driver.service.DriverChargeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/driver-charge")
@RequiredArgsConstructor
@Slf4j
public class DriverChargeController {

    private final DriverChargeService driverChargeService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate Driver Charge", description = "Calculate pickup and delivery charges based on distance")
    public ResponseEntity<DriverChargeCalculationResponseDto> calculateDriverCharge(@Valid @RequestBody DriverChargeCalculationRequestDto requestDto) {

        log.info("POST API called to calculate driver charge | outletId={}, customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());

        return ResponseEntity.ok(driverChargeService.calculateDriverCharge(requestDto));
    }
}