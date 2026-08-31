package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApprovalRequestResponseDTO;
import com.jippy.foodandmart.dto.FmApprovalTransactionResponseDTO;
import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.service.IFmApprovalTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Approval Transactions.
 */
@RestController
@RequestMapping("/api/fm/approval-transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Approval Transactions", description = "Approval Transaction Management APIs")
public class FmApprovalTransactionController {

    private final IFmApprovalTransactionService approvalTransactionService;

    /**
     * Fetches all REJECTED approval transactions.
     *
     * @return List of rejected approval transactions
     */
    @Operation(
            summary = "Get All Rejected Approvals",
            description = "Retrieves all approval transactions with REJECTED status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Rejected approvals retrieved successfully.")
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error.")
    @GetMapping("/getRejectedApprovals")
    public ResponseEntity<FmApiResponse<List<FmApprovalTransactionResponseDTO>>> getRejectedApprovals() {

        log.info("Received request to get all REJECTED approval transactions.");

        List<FmApprovalTransactionResponseDTO> response = approvalTransactionService.getRejectedApprovals();

        log.info("Retrieved {} REJECTED approval transactions.", response.size());

        return ResponseEntity.ok(FmApiResponse.success("Rejected approvals retrieved successfully.", response));
    }

    /**
     * Fetches all PENDING approval requests from approval_request table.
     *
     * @return List of pending approval requests
     */
    @Operation(
            summary = "Get All Pending Approvals",
            description = "Retrieves all approval requests with PENDING status from approval_request table."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pending approvals retrieved successfully.")
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error.")
    @GetMapping("/getPendingApprovals")
    public ResponseEntity<FmApiResponse<List<FmApprovalRequestResponseDTO>>> getPendingApprovals() {

        log.info("Received request to get all PENDING approval requests from approval_request table.");

        List<FmApprovalRequestResponseDTO> response = approvalTransactionService.getPendingApprovals();

        log.info("Retrieved {} PENDING approval requests.", response.size());

        return ResponseEntity.ok(FmApiResponse.success("Pending approvals retrieved successfully.", response));
    }

    /**
     * Fetches all approval transactions.
     *
     * @return List of all approval transactions
     */
    @Operation(
            summary = "Get All Transactions",
            description = "Retrieves all approval transactions regardless of status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "All transactions retrieved successfully.")
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error.")
    @GetMapping("/getAllTransactions")
    public ResponseEntity<FmApiResponse<List<FmApprovalTransactionResponseDTO>>> getAllTransactions() {

        log.info("Received request to get all approval transactions.");

        List<FmApprovalTransactionResponseDTO> response = approvalTransactionService.getAllTransactions();

        log.info("Retrieved {} approval transactions.", response.size());

        return ResponseEntity.ok(FmApiResponse.success("All transactions retrieved successfully.", response));
    }
}
