package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateRequestDTO;
import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.entity.FmApprovalTransaction;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class FmApprovalTransactionMapper {

    public static FmApprovalTransaction toEntity(
            FmApprovalRequest approvalRequest,
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        FmApprovalTransaction transaction = new FmApprovalTransaction();

        transaction.setEntityType(approvalRequest.getEntityType());
        transaction.setEntityId(approvalRequest.getEntityId());
        transaction.setApprovalLevel(approvalRequest.getCurrentLevel());
        transaction.setStatus(requestDTO.getStatus());
        transaction.setApprovedBy(requestDTO.getApproverId());
        transaction.setApprovedAt(LocalDateTime.now());
//        transaction.setUpdatedBy(requestDTO.getApproverId());
//        transaction.setUpdatedAt(LocalDateTime.now());

        if (FmAppConstants.APPROVAL_STATUS_REJECTED
                .equalsIgnoreCase(requestDTO.getStatus())) {
            transaction.setRejectedReason(requestDTO.getRejectedReason());
        }

        return transaction;
    }
}