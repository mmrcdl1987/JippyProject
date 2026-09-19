package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverZoneDto;
import com.jippy.driver.service.DriverZoneService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver/zones")
@RequiredArgsConstructor
@Slf4j
public class DriverZoneController {

    private final DriverZoneService driverZoneService;

    // CREATE ZONE
    @PostMapping
    public ResponseEntity<String> createZone(@Valid @RequestBody DriverZoneDto zoneDto) {

        log.info("API_START | Create driver zone");

        String response = driverZoneService.createZone(zoneDto);

        log.info("API_SUCCESS | Driver zone created");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // GET ZONE BY ID

    @GetMapping("/{zoneId}")
    public ResponseEntity<DriverZoneDto> getZoneById(@PathVariable Integer zoneId) {

        log.info("API_START | Get driver zone | zoneId={}", zoneId);

        DriverZoneDto response = driverZoneService.getZoneById(zoneId);

        log.info("API_SUCCESS | Driver zone fetched | zoneId={}", zoneId);

        return ResponseEntity.ok(response);
    }

    // GET ALL ZONES
    @GetMapping
    public ResponseEntity<List<DriverZoneDto>> getAllZones() {

        log.info("API_START | Get all driver zones");

        List<DriverZoneDto> response = driverZoneService.getAllZones();

        log.info("API_SUCCESS | Driver zones fetched | count={}", response.size());

        return ResponseEntity.ok(response);
    }

    // UPDATE ZONE
    @PutMapping("/{zoneId}")
    public ResponseEntity<String> updateZone(@PathVariable Integer zoneId, @Valid @RequestBody DriverZoneDto zoneDto) {

        log.info("API_START | Update driver zone | zoneId={}", zoneId);

        String response = driverZoneService.updateZone(zoneId, zoneDto);

        log.info("API_SUCCESS | Driver zone updated | zoneId={}", zoneId);

        return ResponseEntity.ok(response);
    }

    // UPDATE ZONE STATUS

    @PatchMapping("/{zoneId}/status")
    public ResponseEntity<String> updateZoneStatus(@PathVariable Integer zoneId, @RequestParam String status) {

        log.info("API_START | Update zone status | zoneId={}, status={}", zoneId, status);

        String response = driverZoneService.updateZoneStatus(zoneId, status);

        log.info("API_SUCCESS | Zone status updated | zoneId={}", zoneId);

        return ResponseEntity.ok(response);
    }
}