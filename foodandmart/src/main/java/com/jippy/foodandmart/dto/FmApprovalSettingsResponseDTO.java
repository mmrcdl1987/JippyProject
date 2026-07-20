package com.jippy.foodandmart.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO after Approval Settings creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmApprovalSettingsResponseDTO {

    private Integer approvalSettingsId;

    private String entityType;

    private String approvalLevel;

    private String approverRole;

    private Integer approverId;

    private Boolean isActive;

    private Integer createdBy;

    private LocalDateTime createdAt;
//    ---------------------------------
    private String workflowType;

    private Integer timeToEscalateInHours;

    private Boolean triggersActivation;

    private Integer requiredApprovalsCount;
}