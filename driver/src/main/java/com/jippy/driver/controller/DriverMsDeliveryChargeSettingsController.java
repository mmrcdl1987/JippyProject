package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverDeliveryChargeSettingsDeleteRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetByIdResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsPaginationResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsSaveRequestDto;
import com.jippy.driver.service.DriverMsDeliveryChargeSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/delivery-charge-settings")
@RequiredArgsConstructor
@Slf4j
public class DriverMsDeliveryChargeSettingsController {

    private final DriverMsDeliveryChargeSettingsService service;


    // ============================================================
    // CREATE + UPDATE
    // ============================================================

    @PostMapping("/save")
    public ResponseEntity<DriverDeliveryChargeSettingsGetByIdResponseDto> save(@RequestBody DriverDeliveryChargeSettingsSaveRequestDto request) {

        log.info("Received request to save driver delivery charge setting. id={}", request != null ? request.getDeliveryChargeSettingId() : null);

        DriverDeliveryChargeSettingsGetByIdResponseDto response = service.save(request);

        log.info("Driver delivery charge setting saved successfully. id={}", response != null ? response.getDeliveryChargeSettingId() : null);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<DriverDeliveryChargeSettingsGetByIdResponseDto> getById(@PathVariable("id") Integer id) {

        log.info("Received request to get driver delivery charge setting. id={}", id);

        DriverDeliveryChargeSettingsGetByIdResponseDto response = service.getById(id);

        log.info("Driver delivery charge setting fetched successfully. id={}", id);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET ALL WITH PAGINATION
    // ============================================================

    @GetMapping("/get-all")
    public ResponseEntity<DriverDeliveryChargeSettingsPaginationResponseDto> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        log.info("Received request to get all driver delivery charge settings. page={}, size={}", page, size);

        DriverDeliveryChargeSettingsPaginationResponseDto response = service.getAll(page, size);

        log.info("Driver delivery charge settings list fetched successfully. page={}, size={}", page, size);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody DriverDeliveryChargeSettingsDeleteRequestDto request) {

        log.info("Received request to delete driver delivery charge setting. id={}", request != null ? request.getDeliveryChargeSettingId() : null);

        service.delete(request);

        log.info("Driver delivery charge setting deleted successfully. id={}", request != null ? request.getDeliveryChargeSettingId() : null);

        return ResponseEntity.ok("Driver delivery charge setting deleted successfully");
    }
}