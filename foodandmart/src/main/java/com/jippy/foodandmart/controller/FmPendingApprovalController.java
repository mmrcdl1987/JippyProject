package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmPendingApprovalRequestDTO;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.service.IFmPendingApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Pending Approval Requests.
 *
 * Provides APIs for fetching pending approval requests
 * assigned to an Approver based on the selected Entity Type.
 */
@RestController
@RequestMapping("/api/fm/pending-approvals")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Pending Approval Controller",
        description = "APIs for fetching Pending Approval Requests."
)
public class FmPendingApprovalController {

    private final IFmPendingApprovalService pendingApprovalService;

    /**
     * Fetches Pending Approval Requests based on
     * Approver Id and Entity Type.
     *
     * Business Flow:
     * 1. Validate Request.
     * 2. Identify Entity Type.
     * 3. Delegate the request to the corresponding
     *  Service method based on the selected Entity Type.
     * 4. Return Pending Approval Requests.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     * @return Pending Approval Requests.
     */
    @Operation(
            summary = "Get Pending Approval Requests By Entity Type",
            description = "Fetches pending approval requests for the specified Approver "
                    + "based on the selected Entity Type. "
                    + "Supported Entity Types are OUTLET, MERCHANT and DRIVER."
    )
    @ApiResponse(responseCode = "200", description = "Pending Approval Requests fetched successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "404", description = "No Pending Approval Requests found.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error.")

    @PostMapping("/getLevel1PendingApprovalRequestsByEntityType")
    public ResponseEntity<FmApiResponse<List<?>>> getPendingApprovalRequestsByEntityType(
            @Valid @RequestBody FmPendingApprovalRequestDTO requestDTO) {

        log.info("Received request to fetch Pending Approval Requests. Approver Id: {}, Entity Type: {}",
                requestDTO.getApproverId(),
                requestDTO.getEntityType());

        List<?> response;

        String successMessage;

        if (FmAppConstants.TYPE_OUTLET.equals(requestDTO.getEntityType())) {
            log.info("Fetching Pending Outlet Approval Requests.");

            response = pendingApprovalService.getPendingOutletApprovalRequests(requestDTO);

            successMessage = "Pending Outlet Approval Requests fetched successfully.";

        }
        else if (FmAppConstants.TYPE_MERCHANT.equals(requestDTO.getEntityType())){
            log.info("Fetching Pending Merchant Approval Requests.");

            response = pendingApprovalService.getPendingMerchantApprovalRequests(requestDTO);

            successMessage = "Pending Merchant Approval Requests fetched successfully.";

        }

//        else if (FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(requestDTO.getEntityType())) {
//
//
//            log.info("Fetching Pending Driver Approval Requests.");
//
////            response = pendingApprovalService.getPendingDriverApprovalRequests(requestDTO);

//             }

        else {

            log.warn("Unsupported Entity Type: {}", requestDTO.getEntityType());

            throw new ResourceNotFoundException(
                    "Unsupported Entity Type: " + requestDTO.getEntityType());
        }

        log.info("Successfully fetched {} Pending {} Approval Request(s).", response.size(),
                requestDTO.getEntityType());

        return ResponseEntity.ok(
                FmApiResponse.success(successMessage, response));
    }
}