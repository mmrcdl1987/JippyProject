package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModesDto;
import com.jippy.customerandorder.entity.CoPaymentModes;
import com.jippy.customerandorder.iservice.IOrderSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/order-settings")
@RequiredArgsConstructor
@Slf4j
public class CoOrderSettingsController {

    private final IOrderSettingsService orderSettingsService;

    @PostMapping
    public ResponseEntity<CoOrderSettingsResponseDto> saveOrUpdate(@Valid @RequestBody CoOrderSettingsRequestDto requestDto) {

        log.info("SAVE OR UPDATE ORDER SETTINGS API START");

        CoOrderSettingsResponseDto response = orderSettingsService.saveOrUpdate(requestDto);

        log.info("SAVE OR UPDATE ORDER SETTINGS API SUCCESS");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getPaymentModeById")
    public ResponseEntity<CoPaymentModesDto> getPaymentModeById(@RequestParam Integer paymentModeId) {

       log.info("GET PAYMENT MODE BY ID API START");

        CoPaymentModesDto response = orderSettingsService.getPaymentModeById(paymentModeId);

        return ResponseEntity.ok(response);
    }

}