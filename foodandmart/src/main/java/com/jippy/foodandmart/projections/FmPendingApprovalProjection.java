package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

/**
 * Projection used by Auto Approval Scheduler.
 *
 * This projection fetches only the required fields
 * from Approval Requests and Approval Settings
 * without loading complete entities.
 */
public interface FmPendingApprovalProjection {

    /**
     * Approver ID configured in Approval Settings
     * for the current Entity Type and Approval Level.
     */
    Integer getApproverId();

    /**
     * Approval Request ID
     */
    Integer getApprovalRequestId();

    /**
     * Entity Type
     */
    String getEntityType();

    /**
     * Entity ID
     */
    Integer getEntityId();

    /**
     * Current Approval Level
     */
    String getCurrentLevel();

    /**
     * Approval Status
     */
    String getStatus();

    /**
     * Created Time
     */
    LocalDateTime getCreatedAt();

    /**
     * Updated Time
     */
    LocalDateTime getUpdatedAt();

    /**
     * Escalation Time (Hours)
     */
    Integer getTimeToEscalateInHours();

    /**
     * Trigger Activation
     */
    Boolean getTriggersActivation();
}