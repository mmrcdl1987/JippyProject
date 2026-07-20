package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating Approval Settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmApprovalSettingsRequestDTO {

    @Schema(description = "Type of entity. Allowed values: MERCHANT, OUTLET, DRIVER.", example = "OUTLET")
    @NotBlank(message = "Entity Type is required")
    @Pattern(regexp = "MERCHANT|OUTLET|DRIVER", message = "Entity Type must be MERCHANT, OUTLET, or DRIVER")
    private String entityType;

    @Schema(description = "Approval level in the approval workflow (e.g., Level 1, Level 2, Level 3).", example = "Level 1")
    @NotBlank(message = "Approval Level is required")
    private String approvalLevel;


    @Schema(description = "Approver role. Allowed values: FLEET_MANAGER, " + "ZONE_MANAGER, DIVISION_MANAGER, GENERAL_MANAGER.", example = "DIVISION_MANAGER")
    @NotBlank(message = "Approver Role is required")
    private String approverRole;

    @Schema(description = "Unique identifier of the approver associated with the specified role.", example = "105")
    @NotNull(message = "Approver Id is required")
    @Positive(message = "Approver Id must be greater than zero")
    private Integer approverId;

    @Schema(description = "Indicates whether the approval configuration is active.", example = "true")
    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @Schema(description = "Unique identifier of the user creating the approval configuration.", example = "1")
    @NotNull(message = "Created By is required")
    @Positive(message = "Created By must be greater than zero")
    private Integer createdBy;

    //    ------------- added new fields ----------------------
    @Schema(description = "Approval workflow type.", example = "CASCADE")
    @NotBlank(message = "Workflow Type is required")
    @Pattern(regexp = "CASCADE|PARALLEL", message = "Workflow Type must be CASCADE or PARALLEL")
    private String workflowType;

    @Schema(description = "Time in hours after which approval request is escalated.", example = "24")
    @Positive(message = "Time To Escalate must be greater than zero")
    private Integer timeToEscalateInHours;

    @Schema(description = "Indicates whether workflow activates immediately.", example = "false")
    @NotNull(message = "Triggers Activation is required")
    private Boolean triggersActivation;

    @Schema(description = "Number of approvals required.", example = "2")
    @Positive(message = "Required Approvals Count must be greater than zero")
    private Integer requiredApprovalsCount;
}