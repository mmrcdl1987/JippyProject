package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import com.jippy.customerandorder.iservice.OrderSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/co/order-settings")
@RequiredArgsConstructor
@Slf4j
public class CoOrderSettingsController {

    private final OrderSettingsService orderSettingsService;

    @PostMapping
    public ResponseEntity<CoOrderSettingsResponseDto> saveOrUpdate(@Valid @RequestBody CoOrderSettingsRequestDto requestDto) {

        log.info("SAVE OR UPDATE ORDER SETTINGS API START");

        CoOrderSettingsResponseDto response = orderSettingsService.saveOrUpdate(requestDto);

        log.info("SAVE OR UPDATE ORDER SETTINGS API SUCCESS");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getPaymentModeById")
    public ResponseEntity<CoPaymentModeResponse> getPaymentModeById(@RequestParam Integer paymentModeId) {

        log.info("GET PAYMENT MODE BY ID API START");

        CoPaymentModeResponse response = orderSettingsService.getPaymentModeById(paymentModeId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getActivePaymentModes")
    public ResponseEntity<List<CoPaymentModeResponse>> getActivePaymentModes() {

        log.info("GET ACTIVE PAYMENT MODEs API START");

        List<CoPaymentModeResponse> response = orderSettingsService.getActivePaymentModes();

        return ResponseEntity.ok(response);
    }
}