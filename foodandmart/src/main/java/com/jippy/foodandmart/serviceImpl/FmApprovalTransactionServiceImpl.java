package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalRequestResponseDTO;
import com.jippy.foodandmart.dto.FmApprovalTransactionResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.entity.FmApprovalTransaction;
import com.jippy.foodandmart.repository.FmApprovalRequestRepository;
import com.jippy.foodandmart.repository.FmApprovalTransactionRepository;
import com.jippy.foodandmart.service.IFmApprovalTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for Approval Transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmApprovalTransactionServiceImpl implements IFmApprovalTransactionService {

    private final FmApprovalTransactionRepository transactionRepository;
    private final FmApprovalRequestRepository requestRepository;

    /**
     * Fetches all REJECTED approval transactions.
     *
     * @return List of rejected approval transactions
     */
    @Override
    public List<FmApprovalTransactionResponseDTO> getRejectedApprovals() {

        log.info("Fetching all REJECTED approval transactions.");

        List<FmApprovalTransaction> transactions =
                transactionRepository.findByStatusIgnoreCaseOrderByApprovedAtDesc(FmAppConstants.APPROVAL_STATUS_REJECTED);

        log.info("Found {} REJECTED approval transactions.", transactions.size());

        return transactions.stream()
                .map(FmApprovalTransactionMapper::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all PENDING approval requests from approval_request table.
     *
     * @return List of pending approval requests
     */
    @Override
    public List<FmApprovalRequestResponseDTO> getPendingApprovals() {

        log.info("Fetching all PENDING approval requests from approval_request table.");

        List<FmApprovalRequest> requests =
                requestRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(FmAppConstants.APPROVAL_STATUS_PENDING);

        log.info("Found {} PENDING approval requests.", requests.size());

        return requests.stream()
                .map(FmApprovalTransactionMapper::toRequestResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all approval transactions.
     *
     * @return List of all approval transactions
     */
    @Override
    public List<FmApprovalTransactionResponseDTO> getAllTransactions() {

        log.info("Fetching all approval transactions.");

        List<FmApprovalTransaction> transactions = transactionRepository.findAll();

        log.info("Found {} approval transactions.", transactions.size());

        return transactions.stream()
                .map(FmApprovalTransactionMapper::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }
}
