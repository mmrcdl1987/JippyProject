package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Approval Transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Approval Transaction Response")
public class FmApprovalTransactionResponseDTO {

    @Schema(description = "Approval Transaction Id", example = "1")
    private Integer approvalTransactionsId;

    @Schema(description = "Entity Type", example = "OUTLET")
    private String entityType;

    @Schema(description = "Entity Id", example = "100")
    private Integer entityId;

    @Schema(description = "Approval Level", example = "Level 1")
    private String approvalLevel;

    @Schema(description = "Approval Status", example = "APPROVED")
    private String status;

    @Schema(description = "Reason for rejection", example = "Documents missing")
    private String rejectedReason;

    @Schema(description = "Approved By User Id", example = "5")
    private Integer approvedBy;

    @Schema(description = "Approved At", example = "2024-01-15T10:30:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Updated By User Id", example = "5")
    private Integer updatedBy;

    @Schema(description = "Updated At", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}
