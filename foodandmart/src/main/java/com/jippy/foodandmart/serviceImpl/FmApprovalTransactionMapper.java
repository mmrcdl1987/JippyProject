package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalRequestResponseDTO;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalTransactionResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.entity.FmApprovalTransaction;

import java.time.LocalDateTime;

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

    public static FmApprovalTransactionResponseDTO toTransactionResponseDTO(FmApprovalTransaction transaction) {
        FmApprovalTransactionResponseDTO dto = new FmApprovalTransactionResponseDTO();
        dto.setApprovalTransactionsId(transaction.getApprovalTransactionsId());
        dto.setEntityType(transaction.getEntityType());
        dto.setEntityId(transaction.getEntityId());
        dto.setApprovalLevel(transaction.getApprovalLevel());
        dto.setStatus(transaction.getStatus());
        dto.setRejectedReason(transaction.getRejectedReason());
        dto.setApprovedBy(transaction.getApprovedBy());
        dto.setApprovedAt(transaction.getApprovedAt());
        dto.setUpdatedBy(transaction.getUpdatedBy());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    public static FmApprovalRequestResponseDTO toRequestResponseDTO(FmApprovalRequest request) {
        FmApprovalRequestResponseDTO dto = new FmApprovalRequestResponseDTO();
        dto.setApprovalRequestId(request.getApprovalRequestId());
        dto.setEntityType(request.getEntityType());
        dto.setEntityId(request.getEntityId());
        dto.setCurrentLevel(request.getCurrentLevel());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setCreatedBy(request.getCreatedBy());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setUpdatedBy(request.getUpdatedBy());
        return dto;
    }
}