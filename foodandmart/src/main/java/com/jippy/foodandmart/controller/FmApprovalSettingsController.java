package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IFmApprovalSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    //============================================================================
    /// Replace Approver with Areas
    ///============================================================================

    /**
     * Replaces the existing Approver with a new Approver for an
     * Approval Setting.
     *
     * <p>
     * Business Rules:
     *
     * 1. Approval Setting must exist.
     * 2. Existing Approver is replaced with the New Approver.
     * 3. All Manager Area mappings assigned to the Old Approver
     *    are transferred to the New Approver.
     * 4. Approval Level, Entity Type and Workflow Type remain unchanged.
     * 5. Updated By and Updated At are recorded.
     */
    @Operation(
            summary = "Replace Approver",
            description = """
                Replaces the existing Approver with a New Approver
                for an Approval Setting.
                
                Business Rules:
                
                1. Approval Setting must exist.
                2. Existing Approver is replaced with the New Approver.
                3. All Manager Area mappings assigned to the Old Approver
                   are transferred to the New Approver.
                4. Approval Level remains unchanged.
                5. Entity Type remains unchanged.
                6. Workflow Type remains unchanged.
                7. Updated By and Updated At are recorded.
                
                Example:
                
                Before
                -------
                Approval Setting:
                Approver Id : 80
                
                Manager Areas:
                80 -> Area 1
                80 -> Area 2
                80 -> Area 3
                
                After
                -----
                Approval Setting:
                Approver Id : 81
                
                Manager Areas:
                81 -> Area 1
                81 -> Area 2
                81 -> Area 3
                """)
    @ApiResponse(
            responseCode = "200",
            description = "Approver replaced successfully.")
    @ApiResponse(
            responseCode = "404",
            description = "Approval Setting not found.")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid Request.")
    @PutMapping("/replaceApproverWithAreas")
    public ResponseEntity<FmUpdateApprovalSettingsResponseDTO> replaceApprover(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Replace Approver Request",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "approvalSettingsId":21,
                                  "approverId":81,
                                  "updatedBy":1
                                }
                                """)))
            @Valid
            @RequestBody
            FmUpdateApprovalSettingsRequestDTO requestDTO) {

        //----------------------------------------------------------
        // Request Received
        //----------------------------------------------------------

        log.info(
                "Received request to replace Approver. Approval Settings Id : {}, New Approver Id : {}",
                requestDTO.getApprovalSettingsId(),
                requestDTO.getApproverId());

        //----------------------------------------------------------
        // Replace Existing Approver
        //----------------------------------------------------------

        FmUpdateApprovalSettingsResponseDTO response =
                service.replaceApproverWithAreas(requestDTO);

        //----------------------------------------------------------
        // Request Completed Successfully
        //----------------------------------------------------------

        log.info(
                "Approver replaced successfully. Approval Settings Id : {}",
                requestDTO.getApprovalSettingsId());

        //----------------------------------------------------------
        // Return Response
        //----------------------------------------------------------

        return ResponseEntity.ok(response);
    }

    //============================================================================
    /// Get All Approval Settings
    ///============================================================================

    @Operation(
            summary = "Get All Approval Settings",
            description = "Retrieves all Approval Settings."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Approval Settings retrieved successfully.")
    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmApprovalSettingsResponseDTO>>> getAllSettings() {

        log.info("Received request to get all Approval Settings.");

        List<FmApprovalSettingsResponseDTO> response = service.getAllSettings();

        log.info("Retrieved {} Approval Settings.", response.size());

        return ResponseEntity.ok(FmApiResponse.success("Approval Settings retrieved successfully.", response));
    }

}