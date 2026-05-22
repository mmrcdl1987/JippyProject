/*
package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.DriverChargeCalculationRequestDto;
import com.jippy.customerandorder.dto.DriverChargeCalculationResponseDto;
import com.jippy.customerandorder.iservice.IDriverChargeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/driver-charge")
@RequiredArgsConstructor
@Slf4j
public class DriverChargeController {

    private final IDriverChargeService driverChargeService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate Driver Charge", description = "Calculate pickup and delivery charges based on distance")
    public ResponseEntity<DriverChargeCalculationResponseDto> calculateDriverCharge(@Valid @RequestBody DriverChargeCalculationRequestDto requestDto) {

        log.info("POST API called to calculate driver charge | outletId={}, customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());

        return ResponseEntity.ok(driverChargeService.calculateDriverCharge(requestDto));
    }
}*/
