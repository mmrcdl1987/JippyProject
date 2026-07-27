package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO returned after successfully changing
 * an Approval Request from REJECTED to PENDING.
 */
@Data
@Schema(name = "FmRejectedApprovalToPendingResponseDTO",
        description = "Response returned after changing a rejected approval request back to pending."
)
public class FmRejectedApprovalToPendingResponseDTO {

    @Schema(
            description = "Approval Request Id",
            example = "8"
    )
    private Integer approvalRequestId;

    @Schema(
            description = "Entity Type",
            example = "DRIVER"
    )
    private String entityType;

    @Schema(
            description = "Entity Id",
            example = "55"
    )
    private Integer entityId;

    @Schema(
            description = "Current Approval Level",
            example = "Level 1"
    )
    private String currentLevel;

    @Schema(
            description = "Updated Approval Request Status",
            example = "PENDING"
    )
    private String status;

    @Schema(
            description = "User Id who performed the update",
            example = "1"
    )
    private Integer updatedBy;

    @Schema(
            description = "Date and time when the Approval Request was updated",
            example = "2026-07-27T15:30:45.123"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Update confirmation message",
            example = "Rejected Approval Request updated to PENDING successfully."
    )
    private String message;
}