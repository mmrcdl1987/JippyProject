package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmApprovalRequestDTO;
import com.jippy.foodandmart.dto.FmLevel1PendingApprovalResponseDTO;
import com.jippy.foodandmart.service.IFmApprovalRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<FmApiResponse<Void>> createApprovalRequest
    (@Valid @RequestBody FmApprovalRequestDTO requestDTO) {

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
    public ResponseEntity<List<FmLevel1PendingApprovalResponseDTO>>getLevel1PendingApprovalRequests(
            @Parameter(description = "Approver User Id", example = "1", required = true)
            @PathVariable
            @Positive(message = "Approver Id must be greater than zero.")
            Integer approverId) {

        log.info("Received request to fetch Level-1 Pending Approval Requests for Approver Id : {}", approverId);

        List<FmLevel1PendingApprovalResponseDTO> response =

        approvalRequestService.getLevel1PendingApprovalRequests(approverId);

        log.info("Successfully fetched {} Pending Approval Requests.", response.size());

        return ResponseEntity.ok(response);

    }
}