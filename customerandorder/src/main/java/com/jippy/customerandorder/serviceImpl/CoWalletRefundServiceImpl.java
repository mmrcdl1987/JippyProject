package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.iservice.CoWalletRefundService;
import com.jippy.customerandorder.repository.CoCustomerWalletRepository;
import com.jippy.customerandorder.repository.CoCustomerWalletTransactionsRepository;
import com.jippy.customerandorder.repository.CoOrderPriceBreakupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoWalletRefundServiceImpl implements CoWalletRefundService {

    private final CoOrderPriceBreakupRepository priceBreakupRepository;
    private final CoCustomerWalletRepository walletRepository;
    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    @Override
    @Transactional
    public BigDecimal processWalletRefund(
            String orderId,
            Integer customerId,
            String cancellationType) {

        log.info(
                "WALLET_REFUND_PROCESS_START | " +
                        "orderId={} | customerId={} | cancellationType={}",
                orderId,
                customerId,
                cancellationType
        );

        // ==========================================
        // CHECK CANCELLATION TYPE
        // ==========================================

        if (!isRefundApplicable(cancellationType)) {

            log.info(
                    "WALLET_REFUND_NOT_APPLICABLE | " +
                            "orderId={} | cancellationType={}",
                    orderId,
                    cancellationType
            );

            return BigDecimal.ZERO;
        }

        // ==========================================
        // PREVENT DUPLICATE REFUND
        // ==========================================

        boolean alreadyRefunded =
                transactionsRepository
                        .existsByOrderIdAndTransactionType(
                                orderId,
                                COConstants.WALLET_REFUND
                        );

        if (alreadyRefunded) {

            log.warn(
                    "WALLET_REFUND_ALREADY_PROCESSED | orderId={}",
                    orderId
            );

            return BigDecimal.ZERO;
        }

        // ==========================================
        // GET WALLET AMOUNT USED
        // ==========================================

        BigDecimal walletAmountUsed = getWalletAmountUsed(orderId);

        if (walletAmountUsed == null ||
                walletAmountUsed.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            log.info(
                    "NO_WALLET_AMOUNT_USED | orderId={}",
                    orderId
            );

            return BigDecimal.ZERO;
        }

        // ==========================================
        // REFUND
        // ==========================================

        return refundToWallet(
                orderId,
                customerId,
                walletAmountUsed
        );
    }

    @Override
    public BigDecimal getWalletAmountUsed(String orderId) {

        CoOrderPriceBreakup priceBreakup = priceBreakupRepository.findByOrderId(orderId);

        if (priceBreakup == null || priceBreakup.getWalletAmount() == null) {

            return BigDecimal.ZERO;
        }
        return priceBreakup.getWalletAmount();
    }

    /**
     * Determine if wallet refund is applicable based on cancellation type
     * - CUSTOMER cancellation: NO wallet refund
     * - OUTLET/DIVER cancellation: YES wallet refund
     */
    private boolean isRefundApplicable(String cancellationType) {
        if (cancellationType == null) {
            return false;
        }

        // Customer cancellation - no wallet refund
        if (COConstants.REJECTION_TYPE_CUSTOMER.equalsIgnoreCase(cancellationType)) {
            return false;
        }

        // Outlet or Driver cancellation - wallet refund applicable
        return COConstants.REJECTION_TYPE_OUTLET.equalsIgnoreCase(cancellationType) ||
               COConstants.REJECTION_TYPE_DRIVER.equalsIgnoreCase(cancellationType) ||
               "DRIVER_REJECTION".equalsIgnoreCase(cancellationType);
    }

    /**
     * Refund amount to customer wallet
     */
    private BigDecimal refundToWallet(
            String orderId,
            Integer customerId,
            BigDecimal refundAmount) {

        log.info(
                "WALLET_REFUND_START | orderId={} | customerId={} | amount={}",
                orderId,
                customerId,
                refundAmount
        );

        // ==========================================
        // GET CUSTOMER WALLET
        // ==========================================

        CoCustomerWallet wallet =
                walletRepository
                        .findByCustomerCustomerId(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Wallet not found for customer id: "
                                                + customerId
                                )
                        );

        BigDecimal currentBalance =
                wallet.getBalanceAmount() != null
                        ? wallet.getBalanceAmount()
                        : BigDecimal.ZERO;

        BigDecimal newBalance =
                currentBalance
                        .add(refundAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        // ==========================================
        // UPDATE WALLET
        // ==========================================

        wallet.setBalanceAmount(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.setUpdatedBy(customerId);

        walletRepository.save(wallet);

        log.info(
                "WALLET_REFUND_BALANCE_UPDATED | " +
                        "customerId={} | oldBalance={} | " +
                        "refund={} | newBalance={}",
                customerId,
                currentBalance,
                refundAmount,
                newBalance
        );

        // ==========================================
        // CREATE REFUND TRANSACTION
        // ==========================================

        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
        transaction.setWalletId(wallet.getWalletId());
        transaction.setOrderId(orderId);
        transaction.setTransactionType(COConstants.WALLET_REFUND);
        transaction.setPoints(0);
        transaction.setAmount(refundAmount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCreatedBy(customerId);

        transactionsRepository.save(transaction);

        log.info(
                "WALLET_REFUND_TRANSACTION_CREATED | " +
                        "orderId={} | customerId={} | amount={}",
                orderId,
                customerId,
                refundAmount
        );

        return refundAmount;
    }
}