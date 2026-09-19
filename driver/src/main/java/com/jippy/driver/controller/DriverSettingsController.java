package com.jippy.driver.controller;
import com.jippy.driver.dto.*;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import com.jippy.driver.service.DriverDeliveryChargeSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<DriverDeliveryChargeSettingsResponseDto> createDriverDeliveryChargeSetting(@Valid @RequestBody DriverDeliveryChargeSettingsRequestDto requestDto) {

        log.info("API START: POST /api/v1/driver-delivery-charge-settings | pickUpRange={}-{}, deliveryRange={}-{}", requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo(), requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        log.debug("Request received | pickPrice={}, deliveryPrice={}", requestDto.getUnitPricePerPickKm(), requestDto.getUnitPricePerDeliverKm());

        DriverDeliveryChargeSettingsResponseDto response = service.createDriverDeliveryChargeSetting(requestDto);

        log.info("API END: Driver delivery charge setting created | id={}, status=201", response.getDeliveryChargeSettingId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}