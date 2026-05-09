package com.jippy.customerandorder.controller;
import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.customerandorder.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.customerandorder.iservice.IDriverDeliveryChargeSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver-delivery-charge-settings")
@RequiredArgsConstructor
@Slf4j
public class DriverDeliveryChargeSettingsController {

    private final IDriverDeliveryChargeSettingsService service;

    @PostMapping
    public ResponseEntity<DriverDeliveryChargeSettingsResponseDto> createDriverDeliveryChargeSetting(
            @Valid @RequestBody DriverDeliveryChargeSettingsRequestDto requestDto) {

        log.info("API START: POST /api/v1/driver-delivery-charge-settings | pickUpRange={}-{}, deliveryRange={}-{}",
                requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo(),
                requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        log.debug("Request received | pickPrice={}, deliveryPrice={}",
                requestDto.getUnitPricePerPickKm(), requestDto.getUnitPricePerDeliverKm());

        DriverDeliveryChargeSettingsResponseDto response =
                service.createDriverDeliveryChargeSetting(requestDto);

        log.info("API END: Driver delivery charge setting created | id={}, status=201",
                response.getDeliveryChargeSettingId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}