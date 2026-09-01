package com.jippy.division.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.DivPayuWebhookService;
import com.jippy.division.service.PayUService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivPayuWebhookServiceImpl implements DivPayuWebhookService {

    private final OrderRefundRepository orderRefundRepository;
    private final PayUService payUService;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper; // Jackson for serializing raw response

    @Override
    @Transactional
    public boolean processPayUWebhook(Map<String, String> payuParams) {

        String txnid = payuParams.get("txnid");
        String status = payuParams.get("status");
        String mihpayid = payuParams.get("mihpayid");
        String bankRefNum = payuParams.get("bank_ref_num");

        log.info("Received PayU Webhook for TxnID: {} | PayUID: {} | Status: {}", txnid, mihpayid, status);

        // 1. Verify response signature
        boolean isValidHash = payUService.verifyResponseHash(payuParams);
        if (!isValidHash) {
            log.error("PayU Webhook signature verification FAILED for TxnID: {}. Possible tampering detected!", txnid);
            return false;
        }
        log.info("PayU Webhook signature verified successfully for TxnID: {}", txnid);

        // 2. Fetch existing transaction record by txnid
        PaymentTransaction transaction = transactionRepository.findByApplicationOrderId(txnid)
                .orElseThrow(() -> {
                    log.error("PayU Webhook processing failed: Transaction record not found in DB for TxnID: {}", txnid);
                    return new RuntimeException("Transaction not found for ID: " + txnid);
                });

        // 3. Track prior success status for idempotency check on downstream calls
        boolean wasAlreadySuccess = "SUCCESS".equalsIgnoreCase(transaction.getPaymentStatus());

        // 4. Update transaction status and gateway metadata
        if ("success".equalsIgnoreCase(status)) {
            transaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_CAPTURED);
            if (!wasAlreadySuccess) {
                log.info("Transitioning TxnID: {} status to SUCCESS via Webhook", txnid);
            }
        } else {
            transaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_CAPTURED_FAILED);
            log.warn("Transitioning TxnID: {} status to FAILED via Webhook | Reason/Status: {}", txnid, status);
        }

        transaction.setGatewayPaymentId(mihpayid);
        transaction.setGatewaySignature(payuParams.get("hash"));

        // Always update bank metadata from Webhook payload if present
        if (bankRefNum != null && !bankRefNum.trim().isEmpty()) {
            transaction.setTxnRrn(bankRefNum);
        }
        if (payuParams.get("bankcode") != null) {
            transaction.setBankName(payuParams.get("bankcode"));
        }
        if (payuParams.get("mode") != null) {
            transaction.setBankAuthCode(payuParams.get("mode"));
        }

        transaction.setUpdatedAt(LocalDateTime.now());

        // 5. Serialize raw JSON payload for audit logging
        try {
            transaction.setGatewayRawResponse(objectMapper.writeValueAsString(payuParams));
        } catch (Exception e) {
            log.warn("Failed to serialize raw PayU webhook params to JSON for TxnID: {}", txnid, e);
        }

        transactionRepository.save(transaction);
        log.info("Successfully persisted PayU Webhook updates & bank reference metadata for TxnID: {} in DB.", txnid);

        // 6. Trigger Order Status Update (Downstream / Feign Service) ONLY IF NOT ALREADY PROCESSED
        if ("success".equalsIgnoreCase(status) && !wasAlreadySuccess) {
            log.info("Triggering downstream order status update for OrderID: {}", txnid);
            // coFeignClient.updateOrderStatus(...);
        } else if (wasAlreadySuccess) {
            log.info("Skipped downstream order status update for TxnID: {} — Order was already processed by verification API.", txnid);
        }

        return true;
    }

    @Transactional
    public boolean processRefundWebhook(Map<String, String> params) {
        String mihpayid = params.get("mihpayid");
        String refundId = params.get("refundTokenId"); // Your var2 (refundTxnId)
        String status = params.get("status");         // "success" or "failure"
        String bankRefNum = params.get("bank_ref_num"); // Refund ARN / UTR

        log.info("PayU Refund Webhook received for RefundTxnID: {} | Status: {}", refundId, status);

        // 1. Fetch Refund Record
        OrderRefund refundTx = orderRefundRepository.findByRefundTransactionsId(refundId)
                .orElseThrow(() -> new RuntimeException("Refund transaction not found: " + refundId));

        // Idempotency guard
        if ("REFUND_SUCCESS".equalsIgnoreCase(refundTx.getRefundStatus())) {
            log.info("Refund {} already marked as REFUND_SUCCESS", refundId);
            return true;
        }

        // 2. Fetch Payment Record
        PaymentTransaction paymentTx = transactionRepository.findByApplicationOrderId(refundTx.getApplicationOrderId())
                .orElseThrow(() -> new RuntimeException("Payment transaction not found for order: " + refundTx.getApplicationOrderId()));

        if ("success".equalsIgnoreCase(status)) {
            refundTx.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
            refundTx.setBankArn(bankRefNum); // Save Acquirer Reference Number (ARN)
            paymentTx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
        } else {
            refundTx.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
            paymentTx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
        }

        refundTx.setUpdatedAt(LocalDateTime.now());
        paymentTx.setUpdatedAt(LocalDateTime.now());

        orderRefundRepository.save(refundTx);
        transactionRepository.save(paymentTx);

        return true;
    }

}
