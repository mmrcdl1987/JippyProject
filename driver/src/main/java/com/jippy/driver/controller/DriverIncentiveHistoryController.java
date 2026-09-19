package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverIncentiveHistoryPageResponseDto;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/driver/incentive-settings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DriverIncentiveHistoryController {

    private final DriverIncentiveSettingsService driverIncentiveSettingsService;

    @GetMapping("/history")
    public ResponseEntity<Page<DriverIncentiveHistoryPageResponseDto>> getIncentiveHistoryPage(

            @RequestParam(required = false) Integer driverId,

            @RequestParam(required = false) String filter,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be negative") Integer page,

            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") Integer size) {

        log.info("Incentive history request: driverId={}, filter={}, " + "startDate={}, endDate={}, page={}, size={}", driverId, filter, startDate, endDate, page, size);

        Page<DriverIncentiveHistoryPageResponseDto> response = driverIncentiveSettingsService.getIncentiveHistoryPage(driverId, filter, startDate, endDate, page, size);

        log.info("Incentive history response: totalElements={}, " + "totalPages={}, currentPage={}, pageSize={}", response.getTotalElements(), response.getTotalPages(), response.getNumber(), response.getSize());

        return ResponseEntity.ok(response);
    }
}