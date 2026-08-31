package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Approval Request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Approval Request Response")
public class FmApprovalRequestResponseDTO {

    @Schema(description = "Approval Request Id", example = "1")
    private Integer approvalRequestId;

    @Schema(description = "Entity Type", example = "OUTLET")
    private String entityType;

    @Schema(description = "Entity Id", example = "100")
    private Integer entityId;

    @Schema(description = "Current Approval Level", example = "Level 1")
    private String currentLevel;

    @Schema(description = "Approval Status", example = "PENDING")
    private String status;

    @Schema(description = "Created At", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Created By User Id", example = "5")
    private Integer createdBy;

    @Schema(description = "Updated At", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated By User Id", example = "5")
    private Integer updatedBy;
}
