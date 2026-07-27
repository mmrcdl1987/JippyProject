package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.entity.FmApprovalTransaction;
import com.jippy.foodandmart.projections.FmPendingApprovalProjection;

import java.time.LocalDateTime;

/**
 * Mapper class for Auto Approval Scheduler.
 *
 * <p>
 * Converts Pending Approval Projection into
 * Approval Transaction Entity.
 */
public class FmAutoApprovalTransactionMapper {

    /**
     * Converts Pending Approval Projection
     * into Approval Transaction Entity.
     * <p>
     * Business Rules:
     * <ul>
     *     <li>Status = AUTO_APPROVED</li>
     *     <li>Approved By = System</li>
     *     <li>Approved At = Current Timestamp</li>
     * </ul>
     *
     * @param approvalRequest Pending Approval Projection
     * @return Approval Transaction Entity
     */
    public static FmApprovalTransaction toEntity(FmPendingApprovalProjection approvalRequest) {

        FmApprovalTransaction transaction = new FmApprovalTransaction();

        //----------------------------------------------------------
        // Copy Approval Request Details
        //----------------------------------------------------------

        transaction.setEntityType(approvalRequest.getEntityType());

        transaction.setEntityId(approvalRequest.getEntityId());

        transaction.setApprovalLevel(approvalRequest.getCurrentLevel());

        //----------------------------------------------------------
        // Auto Approval Details
        //----------------------------------------------------------

        transaction.setStatus(FmAppConstants.APPROVAL_STATUS_AUTO_APPROVED);

        transaction.setRejectedReason(null);

        //----------------------------------------------------------
        // Set Approver configured in Approval Settings
        //----------------------------------------------------------

        transaction.setApprovedBy(approvalRequest.getApproverId());

        transaction.setApprovedAt(LocalDateTime.now());

        transaction.setUpdatedBy(approvalRequest.getApproverId());

        transaction.setUpdatedAt(LocalDateTime.now());

        return transaction;
    }

}