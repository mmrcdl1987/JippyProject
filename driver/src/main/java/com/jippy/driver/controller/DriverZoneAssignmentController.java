package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverZoneAssignmentRequestDto;
import com.jippy.driver.dto.DriverZoneAssignmentResponseDto;
import com.jippy.driver.service.DriverZoneAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zone Assignment To Driver API", description = "APIs for assigning zone to driver")
public class DriverZoneAssignmentController {

    private final DriverZoneAssignmentService assignmentService;

    @Operation(summary = "Assign zone to driver using latitude and longitude")
    @PostMapping("/zoneAssignmentToDriver")
    public ResponseEntity<DriverZoneAssignmentResponseDto> assignZoneToDriver
            (@Valid @RequestBody DriverZoneAssignmentRequestDto requestDto) {

        log.info("Received request for zone assignment");

        DriverZoneAssignmentResponseDto AssignDriverResponse =
                assignmentService.assignZoneToDriver(requestDto);

        return ResponseEntity.ok(AssignDriverResponse);
    }
}