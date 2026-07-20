package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing Approval Requests.
 *
 * Stores every entity that enters the approval workflow.
 */
@Entity
@Table(name = "approval_requests", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmApprovalRequest {

    /**
     * Primary Key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_request_id")
    private Integer approvalRequestId;

    /**
     * Entity Type.
     * Example:
     * MERCHANT
     * OUTLET
     * DRIVER
     */
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * Entity Id.
     *
     * Merchant -> merchantId
     * Outlet -> outletId
     * Driver -> driverId
     */
    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    /**
     * Current Approval Level.
     * Example:
     * Level 1
     */
    @Column(name = "current_level", nullable = false)
    private String currentLevel;

    /**
     * Approval Status.
     * Example:
     * PENDING
     * APPROVED
     * REJECTED
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Created Date & Time.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Created By User Id.
     */
    @Column(name = "created_by")
    private Integer createdBy;

    /**
     * Updated Date & Time.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Updated By User Id.
     */
    @Column(name = "updated_by")
    private Integer updatedBy;

}