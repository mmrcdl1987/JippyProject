package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApprovalRequestResponseDTO;
import com.jippy.foodandmart.dto.FmApprovalTransactionResponseDTO;

import java.util.List;

/**
 * Service interface for Approval Transactions.
 */
public interface IFmApprovalTransactionService {

    /**
     * Fetches all REJECTED approval transactions.
     *
     * @return List of rejected approval transactions
     */
    List<FmApprovalTransactionResponseDTO> getRejectedApprovals();

    /**
     * Fetches all PENDING approval requests from approval_request table.
     *
     * @return List of pending approval requests
     */
    List<FmApprovalRequestResponseDTO> getPendingApprovals();

    /**
     * Fetches all approval transactions.
     *
     * @return List of all approval transactions
     */
    List<FmApprovalTransactionResponseDTO> getAllTransactions();
}
