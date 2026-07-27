package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IFmApprovalRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Approval Requests.
 */
@RestController
@RequestMapping("/api/fm/approval-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Approval Requests", description = "Approval Request Management APIs.")
public class FmApprovalRequestController {

    /**
     * Approval Request Service.
     */
    private final IFmApprovalRequestService approvalRequestService;

    /**
     * Creates Approval Request.
     * <p>
     * This API is mainly used by other microservices
     * -------(Driver Microservice)------ through Feign Client.
     *
     * @param requestDTO Approval Request.
     * @return Success Response.
     */
    @Operation(summary = "Create Approval Request", description = "APIs related to Approval Workflow")
    @ApiResponse(responseCode = "200", description = "Approval Request created successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid Request.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error.")

    @PostMapping("/createApprovalRequest")
    public ResponseEntity<FmApiResponse<Void>> createApprovalRequest(@Valid @RequestBody FmApprovalRequestDTO requestDTO) {

        log.info("Received request to create Approval Request for Entity Type: {}, Entity Id: {}", requestDTO.getEntityType(), requestDTO.getEntityId());

        approvalRequestService.createApprovalRequest(requestDTO);

        log.info("Approval Request created successfully.");

        return ResponseEntity.ok(FmApiResponse.success("Approval Request created successfully.", null));
    }

//    ---------------------------------------------------------------------------------------------

    /**
     * ======================================================
     * Get Level-1 Pending Approval Requests
     * ======================================================
     * <p>
     * This API fetches all pending Level-1 Approval Requests
     * assigned to an Approver.
     * <p>
     * Flow
     * ----
     * <p>
     * 1. Validate Approver
     * <p>
     * 2. Fetch Approval Settings
     * <p>
     * 3. Validate Manager Areas
     * <p>
     * 4. Fetch Pending Approval Requests
     * <p>
     * 5. Based on Entity Type
     * <p>
     * OUTLET
     * MERCHANT
     * DRIVER
     * <p>
     * fetch complete details.
     * <p>
     * ======================================================
     */
    @Operation(summary = "Fetch Level-1 Pending Approval Requests", description = "Returns all pending Level-1 approval requests assigned to the given approver.")
    @ApiResponse(responseCode = "200", description = "Pending Approval Requests fetched successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid Approver Id.")
    @ApiResponse(responseCode = "404", description = "Approval Settings or Pending Requests not found.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    @GetMapping("/getPendingLevelApprovalRequestsByApproverId/{approverId}")
    public ResponseEntity<List<FmLevel1PendingApprovalResponseDTO>> getLevel1PendingApprovalRequests(@Parameter(description = "Approver User Id", example = "1", required = true) @PathVariable @Positive(message = "Approver Id must be greater than zero.") Integer approverId) {

        log.info("Received request to fetch Level-1 Pending Approval Requests for Approver Id : {}", approverId);

        List<FmLevel1PendingApprovalResponseDTO> response =

                approvalRequestService.getLevel1PendingApprovalRequests(approverId);

        log.info("Successfully fetched {} Pending Approval Requests.", response.size());

        return ResponseEntity.ok(response);

    }
//    ================================  Rejected Approval Requests.===================================

    /**
     * Fetches all Rejected Approval Requests.
     *
     * <p>
     * This API retrieves all approval transactions where
     * the approval status is REJECTED.
     *
     * <p>
     * The response contains:
     * <p>
     * 1. Approval Transaction details.
     * 2. Approval Request details.
     * 3. Rejection reason and rejection information.
     * 4. Basic entity details based on Entity Type.
     *
     * <p>
     * Supported Entity Types:
     * <p>
     * - OUTLET   : Details are fetched from the Outlet table.
     * - MERCHANT : Details are fetched from the Merchant table.
     * - DRIVER   : Details are fetched from Driver Microservice using Feign.
     *
     * @return List of all rejected approval details
     */
    @Operation(summary = "Get All Rejected Approvals", description = """
            Fetches all rejected approval requests.
            
            The API checks Approval Transactions where status is REJECTED
            and returns the corresponding Approval Request details.
            
            Entity details are fetched based on Entity Type:
            
            OUTLET:
            - Outlet Name
            - Outlet Phone
            - Alternate Outlet Phone
            - Outlet Email
            - Outlet Image
            - Approval Status
            
            MERCHANT:
            - Merchant Name
            - Merchant Phone
            - Merchant Email
            - Profile Image
            - Approval Status
            
            DRIVER:
            - Driver First Name and Last Name
            - Driver Phone Number
            - Driver Email
            - Driver Profile Image
            - Driver Approval Status
            
            Rejected approvals are returned with the latest
            rejected records first.
            """)
    @ApiResponse(responseCode = "200", description = "Rejected Approval Requests fetched successfully")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    @GetMapping("/getAllRejectedApprovals")
    public ResponseEntity<List<FmRejectedApprovalResponseDTO>> getAllRejectedApprovals() {

        log.info("Received request to fetch all Rejected Approval Requests.");

        //----------------------------------------------------------
        // Fetch All Rejected Approval Requests
        //----------------------------------------------------------

        List<FmRejectedApprovalResponseDTO> response = approvalRequestService.getAllRejectedApprovals();

        //----------------------------------------------------------
        // Request Completed Successfully
        //----------------------------------------------------------

        log.info("Successfully fetched {} Rejected Approval Requests.", response.size());

        //----------------------------------------------------------
        // Return Response
        //----------------------------------------------------------

        return ResponseEntity.ok(response);
    }
//    =========================================================================================

    /**
     * Updates a REJECTED Approval Request back to PENDING.
     *
     * <p>
     * Business Rule:
     * <p>
     * Only Approval Requests currently having status REJECTED
     * can be changed back to PENDING.
     *
     * <p>
     * The following values remain unchanged:
     * <p>
     * - Entity Type
     * - Entity Id
     * - Current Approval Level
     * <p>
     * Only the status and audit information are updated.
     */
    @Operation(summary = "Update Rejected Approval Request to Pending", description = """
            Changes an Approval Request from REJECTED status back to PENDING.
            
            Business Rules:
            
            1. Approval Request must exist.
            2. Current status must be REJECTED.
            3. Only REJECTED requests can be changed to PENDING.
            4. Current Approval Level remains unchanged.
            5. Entity Type remains unchanged.
            6. Entity Id remains unchanged.
            7. Updated At and Updated By are updated.
            
            Example:
            
            Before:
            Approval Request Id : 8
            Entity Type         : DRIVER
            Entity Id           : 55
            Current Level       : Level 1
            Status              : REJECTED
        --------------------------------------
            After:
            Approval Request Id : 8
            Entity Type         : DRIVER
            Entity Id           : 55
            Current Level       : Level 1
            Status              : PENDING
            """)


    @ApiResponse(responseCode = "200", description = "Rejected Approval Request updated to PENDING successfully")

    @ApiResponse(responseCode = "400", description = "Approval Request is not currently REJECTED")

    @ApiResponse(responseCode = "404", description = "Approval Request not found")

    @ApiResponse(responseCode = "500", description = "Internal Server Error")

    @PutMapping("/updateRejectedApprovalsToPending")
    public ResponseEntity<FmRejectedApprovalToPendingResponseDTO> updateRejectedApprovalsToPending
    (@Valid @io.swagger.v3.oas.annotations.parameters.RequestBody
            (description = "Rejected Approval Request update details", required = true,
             content = @Content(examples = @ExampleObject(value = """
            {
              "approvalRequestId": 8,
              "updatedBy": 1
            }
            """))) @RequestBody FmRejectedApprovalToPendingRequestDTO requestDTO) {

        log.info("Received request to update Rejected Approval Request to PENDING. " + "Approval Request Id : {}", requestDTO.getApprovalRequestId());

        //----------------------------------------------------------
        // Update REJECTED Approval Request → PENDING
        //----------------------------------------------------------

        FmRejectedApprovalToPendingResponseDTO response = approvalRequestService.updateRejectedApprovalsToPending(requestDTO);

        //----------------------------------------------------------
        // Request Completed Successfully
        //----------------------------------------------------------

        log.info("Rejected Approval Request updated to PENDING successfully. " + "Approval Request Id : {}", requestDTO.getApprovalRequestId());

        //----------------------------------------------------------
        // Return 200 OK
        //----------------------------------------------------------

        return ResponseEntity.ok(response);
    }
}