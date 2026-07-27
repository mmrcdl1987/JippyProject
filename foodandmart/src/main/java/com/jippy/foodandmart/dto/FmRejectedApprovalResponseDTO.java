package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO used for returning rejected approval details.
 *
 * <p>
 * Contains:
 * 1. Approval Transaction details.
 * 2. Approval Request details.
 * 3. Basic details of OUTLET / MERCHANT / DRIVER
 *    based on the Entity Type.
 */
@Getter
@Setter
@Schema(description = "Rejected Approval Details")
public class FmRejectedApprovalResponseDTO {

    //----------------------------------------------------------
    // Approval Transaction Details
    //----------------------------------------------------------

    @Schema(description = "Approval Transaction Id", example = "41")
    private Integer approvalTransactionsId;

    @Schema(description = "Entity Type", example = "OUTLET")
    private String entityType;

    @Schema(description = "Entity Id", example = "226")
    private Integer entityId;

    @Schema(description = "Approval Level", example = "Level 1")
    private String approvalLevel;

    @Schema(description = "Approval Status", example = "REJECTED")
    private String status;

    @Schema(
            description = "Reason provided while rejecting the approval",
            example = "Required documents are missing"
    )
    private String rejectedReason;

    //----------------------------------------------------------
    // Approval Request Details
    //----------------------------------------------------------

    @Schema(description = "Approval Request Id", example = "5")
    private Integer approvalRequestId;

    //----------------------------------------------------------
    // Common Entity Details
    //----------------------------------------------------------

    @Schema(
            description = "Entity Name. Outlet Name / Merchant Name / Driver Name",
            example = "Jippy Food Outlet"
    )
    private String entityName;

    @Schema(description = "Entity Email", example = "outlet@gmail.com")
    private String email;

    @Schema(description = "Primary Phone Number", example = "9876543210")
    private String phone;

    @Schema(description = "Alternate Outlet Phone Number")
    private String alternatePhone;

    @Schema(description = "Entity Profile/Image URL")
    private String profilePicUrl;

    @Schema(description = "Entity Approval Status", example = "false")
    private Boolean approved;

    //----------------------------------------------------------
    // Rejection Information
    //----------------------------------------------------------

    @Schema(description = "Rejected By Approver Id", example = "11")
    private Integer rejectedBy;

    @Schema(description = "Rejected Date and Time")
    private LocalDateTime rejectedAt;
}