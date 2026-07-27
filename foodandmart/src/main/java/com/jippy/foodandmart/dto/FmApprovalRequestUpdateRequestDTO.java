package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * Request DTO used to Approve or Reject Approval Requests.
 */
@Data
@Schema(description = "Request DTO for Updating Approval Requests")
public class FmApprovalRequestUpdateRequestDTO {

    @NotEmpty(message = "Approval Request IDs cannot be empty.")
    @Schema(description = "List of Approval Request IDs", example = "[2,3,4]")
    private List<Integer> approvalRequestIds;

    @NotNull(message = "Status is required.")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Status must be either APPROVED or REJECTED.")
    @Schema(description = "Approval Status", example = "APPROVED",
            allowableValues = {"APPROVED", "REJECTED"})
    private String status;

    @Schema(description = "Reason for rejection. Required only when status is REJECTED.",
            example = "Invalid KYC Documents")
    private String rejectedReason;

    @NotNull(message = "Approver ID is required.")
    @Schema(description = "Approver Employee ID", example = "14")
    private Integer approverId;
}