package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverZoneAssignmentRequestDto;
import com.jippy.driver.dto.DriverZoneAssignmentResponseDto;
import com.jippy.driver.dto.ZoneStatusToggleRequestDto;
import com.jippy.driver.service.DriverZoneAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zone Assignment To Driver API", description = "APIs for assigning zone to driver")
public class DriverZoneAssignmentController {

    private final DriverZoneAssignmentService assignmentService;

    @Operation(summary = "Assign zone to driver using latitude and longitude")
    @PostMapping("/zoneAssignmentToDriver")
    public ResponseEntity<DriverZoneAssignmentResponseDto> assignZoneToDriver(@Valid @RequestBody DriverZoneAssignmentRequestDto requestDto) {

        log.info("Received request for zone assignment");

        DriverZoneAssignmentResponseDto AssignDriverResponse = assignmentService.assignZoneToDriver(requestDto);

        return ResponseEntity.ok(AssignDriverResponse);
    }

    //    ======================================================================================
//    ======================================================================================
    @Operation(summary = "Toggle Zone Status", description = "Enables or disables a zone using Y or N status.")
    @ApiResponse(responseCode = "200", description = "Zone status updated successfully.")
    @ApiResponse(responseCode = "400", description = "Status must be either Y or N.")
    @ApiResponse(responseCode = "404", description = "Zone not found.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    @PutMapping("/UpdateStatusToggleForZone")
    public ResponseEntity<String> statusToggleForZone(@Valid @RequestBody ZoneStatusToggleRequestDto requestDTO) {

        log.info("[ZONE STATUS] Received status toggle request | zoneId={} | status={}", requestDTO.getZoneId(), requestDTO.getStatus());

        String message = assignmentService.statusToggleForZone(requestDTO);

        log.info("[ZONE STATUS] Status toggle completed | zoneId={}", requestDTO.getZoneId());

        return ResponseEntity.ok(message);
    }
}