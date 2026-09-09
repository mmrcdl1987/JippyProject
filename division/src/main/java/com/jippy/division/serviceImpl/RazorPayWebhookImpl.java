package com.jippy.division.serviceImpl;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.RazorPayWebhookService;
import com.razorpay.Utils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorPayWebhookImpl implements RazorPayWebhookService {

    private final TransactionRepository transactionRepository;
    private final OrderRefundRepository refundRepository;

    @Value("${razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    @Override
    public ResponseEntity<?> handleRazorpayWebhook(String payload, String signature) {
        log.info("Received Razorpay webhook: payload={}, signature={}", payload, signature);

        // 1. ALWAYS validate the X-Razorpay-Signature here using your webhook secret to prevent spoofing
        try {
           /* boolean isValid = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
            if (!isValid) {
                // Return a 400 Bad Request if the signature doesn't match
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }*/
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying signature: " + e.getMessage());
        }


        JSONObject json = new JSONObject(payload);
        String eventType = json.getString("event");

        if ("payment.captured".equals(eventType) || "order.paid".equals(eventType)) {
            JSONObject paymentEntity = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String rzpPaymentId = paymentEntity.getString("id");
            String rzpOrderId = paymentEntity.getString("order_id");

            // Extract the critical bank transaction reference
            JSONObject acquirerData = paymentEntity.getJSONObject("acquirer_data");
            String bankRrn = acquirerData.optString("rrn", acquirerData.optString("upi_transaction_id", null));
            String authCode = acquirerData.optString("auth_code", null);

            // 2. Persist the tracking strings to your ledger
            transactionRepository.findByGatewayOrderId(rzpOrderId).ifPresent(tx -> {
                tx.setGatewayPaymentId(rzpPaymentId);
                tx.setTxnRrn(bankRrn);
                tx.setBankAuthCode(authCode);
                tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_CAPTURED);
                transactionRepository.save(tx);
            });

            log.info("Payment captured for Razorpay Order ID: {}, Payment ID: {}, Bank RRN: {}, Auth Code: {}", rzpOrderId, rzpPaymentId, bankRrn, authCode);
            return ResponseEntity.ok().body("Webhook processed successfully");

        }
        if ("refund.processed".equals(eventType)) {
            JSONObject refundEntity = json.getJSONObject("payload")
                    .getJSONObject("refund")
                    .getJSONObject("entity");

            String rzpRefundId = refundEntity.getString("id");
            String rzpPaymentId = refundEntity.getString("payment_id");
            String status = refundEntity.getString("status"); // e.g., "processed"

            // Extract the bank's actual tracking number for this refund
            JSONObject acquirerData = refundEntity.optJSONObject("acquirer_data");
            String bankArn = (acquirerData != null) ? acquirerData.optString("arn", null) : null;

            if ("processed".equals(status)) {
                refundRepository.findByGatewayRefundId(rzpRefundId).ifPresent(refund -> {
                    refund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    // 2. Add the bank's tracking code (Add this field to your OrderRefund entity!)
                    refund.setBankArn(bankArn);
                    refundRepository.save(refund);
                });
                log.info("Refund processed for Razorpay Refund ID: {}, Payment ID: {}, Bank ARN: {}", rzpRefundId, rzpPaymentId, bankArn);
                return ResponseEntity.ok().body("Webhook processed successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unhandled event type: " + eventType);
    }
}
