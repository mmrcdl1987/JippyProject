package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.AvailabilityActionRequestDto;
import com.jippy.foodandmart.dto.CreateOutletUnavailabilityRequestDto;
import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.service.OutletUnavailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/outlet-unavailability")
@RequiredArgsConstructor
public class OutletUnavailabilityController {

    private final OutletUnavailabilityService unavailabilityService;

    /**
     * Handles:
     * 1. Temporary close
     * 2. Permanent close
     */
    @PostMapping
    public ResponseEntity<FmApiResponse<Void>> createUnavailability(@Valid @RequestBody CreateOutletUnavailabilityRequestDto requestDto) {

        log.info("CREATE_UNAVAILABILITY_API START | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        unavailabilityService.createUnavailability(requestDto);

        log.info("CREATE_UNAVAILABILITY_API SUCCESS | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success(FmAppConstants.MSG_SUCCESS, null));
    }

    /**
     * Restore availability immediately.
     */
    @PatchMapping("/restore")
    public ResponseEntity<FmApiResponse<Void>> restoreAvailability(@Valid @RequestBody AvailabilityActionRequestDto requestDto) {

        log.info("RESTORE_AVAILABILITY_API START | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        unavailabilityService.restoreAvailability(requestDto);

        log.info("RESTORE_AVAILABILITY_API SUCCESS | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        return ResponseEntity.status(HttpStatus.OK).body(FmApiResponse.success(FmAppConstants.MSG_SUCCESS, null));
    }
}