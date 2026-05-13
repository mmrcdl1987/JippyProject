package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoDriverZoneAssignmentRequestDto;
import com.jippy.customerandorder.dto.CoDriverZoneAssignmentResponseDto;
import com.jippy.customerandorder.iservice.CoDriverZoneAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zone Assignment To Driver API", description = "APIs for assigning zone to driver")
public class CoDriverZoneAssignmentController {

    private final CoDriverZoneAssignmentService assignmentService;

    @Operation(summary = "Assign zone to driver using latitude and longitude")
    @PostMapping("/zoneAssignmentToDriver")
    public ResponseEntity<CoDriverZoneAssignmentResponseDto> assignZoneToDriver(@Valid @RequestBody CoDriverZoneAssignmentRequestDto requestDto) {

        log.info("Received request for zone assignment");

        CoDriverZoneAssignmentResponseDto AssignDriverResponse = assignmentService.assignZoneToDriver(requestDto);

        return ResponseEntity.ok(AssignDriverResponse);
    }
}