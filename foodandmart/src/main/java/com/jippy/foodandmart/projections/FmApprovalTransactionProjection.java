package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

public interface FmApprovalTransactionProjection {

    Integer getApprovalTransactionsId();

    String getEntityType();

    Integer getEntityId();

    String getApprovalLevel();

    String getStatus();

    String getRejectedReason();

    Integer getApprovedBy();

    LocalDateTime getApprovedAt();

    Integer getUpdatedBy();

    LocalDateTime getUpdatedAt();

    String getApproverName();
}