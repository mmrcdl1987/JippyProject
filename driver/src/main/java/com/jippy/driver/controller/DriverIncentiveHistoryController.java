package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverIncentiveHistoryPageResponseDto;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/driver/incentive-settings")
@RequiredArgsConstructor
@Slf4j
public class DriverIncentiveHistoryController {

    private final DriverIncentiveSettingsService driverIncentiveSettingsService;

    @GetMapping("/history/page")
    public ResponseEntity<Page<DriverIncentiveHistoryPageResponseDto>> getIncentiveHistoryPage(@RequestParam(required = false) Integer driverId, @RequestParam(required = false) String filter, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {

        log.info("Incentive history request: driverId={}, filter={}, startDate={}, endDate={}, page={}, size={}", driverId, filter, startDate, endDate, page, size);

        Page<DriverIncentiveHistoryPageResponseDto> response = driverIncentiveSettingsService.getIncentiveHistoryPage(driverId, filter, startDate, endDate, page, size);

        log.info("Incentive history response: totalElements={}, totalPages={}, currentPage={}, pageSize={}", response.getTotalElements(), response.getTotalPages(), response.getNumber(), response.getSize());

        return ResponseEntity.ok(response);
    }
}