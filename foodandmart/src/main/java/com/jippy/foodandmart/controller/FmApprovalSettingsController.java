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

    @Operation(
            summary = "Create Approval Settings",
            description = """
                Creates a new Approval Setting for an Entity.

                DUPLICATE VALIDATION:

                An Approval Setting is considered DUPLICATE only when
                all the following fields are the same:

                1. Entity Type
                2. Approval Level
                3. Approver Id
                4. Workflow Type

                ALLOWED SCENARIOS:

                1. Same Entity Type + Same Approval Level
                   + Different Approver Id + Same Workflow Type
                   -> ALLOWED

                2. Same Entity Type + Different Approval Level
                   + Same Approver Id + Same Workflow Type
                   -> ALLOWED

                3. Different Entity Type + Same Approval Level
                   + Same Approver Id + Same Workflow Type
                   -> ALLOWED

                4. Same Entity Type + Same Approval Level
                   + Same Approver Id + Different Workflow Type
                   -> ALLOWED

                5. Same Entity Type + Different Approval Level
                   + Different Approver Id + Different Workflow Type
                   -> ALLOWED

                6. Different Entity Type + Different Approval Level
                   + Same Approver Id + Same Workflow Type
                   -> ALLOWED

                7. Different Entity Type + Same Approval Level
                   + Different Approver Id + Same Workflow Type
                   -> ALLOWED

                8. Different Entity Type + Same Approval Level
                   + Same Approver Id + Different Workflow Type
                   -> ALLOWED

                NOT ALLOWED:

                Same Entity Type + Same Approval Level
                + Same Approver Id + Same Workflow Type
                -> DUPLICATE

                Example Existing Configuration:
                OUTLET | Level 1 | Approver 1 | CASCADE

                Examples:

                OUTLET | Level 1 | 1   | CASCADE  -> DUPLICATE
                OUTLET | Level 1 | 110 | CASCADE  -> ALLOWED
                OUTLET | Level 2 | 1   | CASCADE  -> ALLOWED
                MERCHANT | Level 1 | 1 | CASCADE  -> ALLOWED
                OUTLET | Level 1 | 1   | PARALLEL -> ALLOWED
                DRIVER | Level 1 | 1   | CASCADE  -> ALLOWED
                """
    )
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