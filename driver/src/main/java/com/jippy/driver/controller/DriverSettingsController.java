package com.jippy.driver.controller;


import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import com.jippy.driver.service.DriverDeliveryChargeSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@Slf4j
public class DriverSettingsController {

    private final DriverDeliveryChargeSettingsService service;
    private final DriverIncentiveSettingsService incentiveSettingsService;

    @PostMapping
    public ResponseEntity<DriverDeliveryChargeSettingsResponseDto> createDriverDeliveryChargeSetting
            (@Valid @RequestBody DriverDeliveryChargeSettingsRequestDto requestDto) {

        log.info("API START: POST /api/v1/driver-delivery-charge-settings | pickUpRange={}-{}, deliveryRange={}-{}", requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo(), requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        log.debug("Request received | pickPrice={}, deliveryPrice={}", requestDto.getUnitPricePerPickKm(), requestDto.getUnitPricePerDeliverKm());

        DriverDeliveryChargeSettingsResponseDto response = service.createDriverDeliveryChargeSetting(requestDto);

        log.info("API END: Driver delivery charge setting created | id={}, status=201", response.getDeliveryChargeSettingId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Operation(summary = "Create or Update Incentive", description = "If ID is null → create, else update existing incentive")
    @PostMapping("/CreateOrUpdateIncentives")
    public ResponseEntity<DriverIncentiveSettingsDto> saveOrUpdate(@Valid @RequestBody DriverIncentiveSettingsDto dto) {

        log.info("Save/Update Incentives request: {}", dto);

        DriverIncentiveSettingsDto response = incentiveSettingsService.saveOrUpdateIncentives(dto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getDriverIncentiveHistory")
    @Operation(summary = "Get Driver Incentive History"
            ,description = "Get incentive history for a driver based on filter" +
            " (currentMonth, ALL) filter value can be monthly or " +
            "ALL ex:filter=all/currentMonth")
    public Page<DriverIncentiveHistoryResponseDto >getDriverIncentiveHistory
            (@RequestParam Integer driverId, @RequestParam String filter,
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "2") int size) {
        log.info("Get Driver Incentive History request | driverId={}, filter={}", driverId, filter);

        log.info("to get incentive details filter value can be monthly or ALL");
        return incentiveSettingsService.getDriverIncentiveHistory(driverId, filter,page,
                size);
    }
}
