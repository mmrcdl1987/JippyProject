package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing Approval Transaction History.
 *
 * This table stores every approval/rejection action
 * performed by an approver.
 */
@Data
@Entity
@Table(name = "approval_transactions", schema = "jippy_fm")
public class FmApprovalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_transactions_id")
    private Integer approvalTransactionsId;
    /**
     * Entity Type
     * Example:
     * OUTLET
     * MERCHANT
     * DRIVER
     */
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    /**
     * Approval Level
     * Example:
     * Level 1
     */
    @Column(name = "approval_level")
    private String approvalLevel;
    /**
     * Approval Status
     * APPROVED / REJECTED
     */
    @Column(name = "status")
    private String status;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}