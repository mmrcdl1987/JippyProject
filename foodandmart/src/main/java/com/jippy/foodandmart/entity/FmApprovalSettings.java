package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing Approval Settings configuration.
 */
@Entity
@Table(name = "approval_settings", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmApprovalSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_settings_id")
    private Integer approvalSettingsId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "approval_level", nullable = false)
    private String approvalLevel;

    @Column(name = "approver_role", nullable = false)
    private String approverRole;

    @Column(name = "approver_id", nullable = false)
    private Integer approverId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    ---------------Newly Added Feilds------------------------
    /**
     * Approval workflow type.
     * Example: CASCADE, PARALLEL.
     */
    @Column(name = "workflow_type", nullable = false)
    private String workflowType;

    /**
     * Time in hours after which the approval
     * request should be escalated.
     */
    @Column(name = "time_to_escalate_in_hours")
    private Integer timeToEscalateInHours;

    /**
     * Indicates whether approval workflow
     * gets activated immediately.
     */
    @Column(name = "triggers_activation")
    private Boolean triggersActivation;

    /**
     * Number of approvals required
     * before the workflow is completed.
     */
    @Column(name = "required_approvals_count")
    private Integer requiredApprovalsCount;
}