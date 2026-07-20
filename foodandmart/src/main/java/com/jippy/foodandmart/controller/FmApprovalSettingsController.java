package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;
import com.jippy.foodandmart.service.IFmApprovalSettingsService;
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

/**
 * REST Controller for Approval Settings.
 */
@RestController
@RequestMapping("/api/fm/approval-settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Approval Settings", description = "Approval Settings Management APIs")
public class FmApprovalSettingsController {

    private final IFmApprovalSettingsService service;

    @Operation(summary = "Create Approval Settings")
    @PostMapping("/createApproval")
    public ResponseEntity<FmApiResponse<FmApprovalSettingsResponseDTO>> createApproval(
            @Valid @RequestBody FmApprovalSettingsRequestDTO requestDTO){


        log.info("Received request to create Approval Settings.");

        FmApprovalSettingsResponseDTO response = service.createApproval(requestDTO);

        log.info("Approval Settings created successfully.");

        return ResponseEntity.ok(FmApiResponse.success("Approval Settings created successfully.",
                        response
                )
        );

    }

}