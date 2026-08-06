package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO after updating Approval Settings.
 */
@Data
@Schema(description = "Response DTO for updated Approval Settings.")
public class FmUpdateApprovalSettingsResponseDTO {

    /**
     * Approval Settings Id.
     */
    @Schema(example = "21")
    private Integer approvalSettingsId;

    /**
     * Entity Type.
     */
    @Schema(example = "DRIVER")
    private String entityType;

    /**
     * Approval Level.
     */
    @Schema(example = "Level 3")
    private String approvalLevel;

    @Schema(
            description = "Approver Role",
            example = "ZONE_MANAGER")
    private String approverRole;

    @Schema(
            description = "Approval Workflow Type",
            example = "CASCADE")
    private String workflowType;

    /**
     * Previous Approver Id.
     */
    @Schema(example = "80")
    private Integer oldApproverId;

    /**
     * Updated Approver Id.
     */
    @Schema(example = "81")
    private Integer newApproverId;

    /**
     * Updated By.
     */
    @Schema(example = "1")
    private Integer updatedBy;

    /**
     * Updated Time.
     */
    @Schema(example = "2026-08-01T10:30:45")
    private LocalDateTime updatedAt;

    /**
     * Success Message.
     */
    @Schema(example = "Approval Settings updated successfully.")
    private String message;
}