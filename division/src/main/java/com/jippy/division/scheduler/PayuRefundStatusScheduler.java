package com.jippy.division.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.PayUService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayuRefundStatusScheduler {

    private final TransactionRepository transactionRepository;
    private final OrderRefundRepository refundRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payu.merchant-key}")
    private String payUMerchantKey;

    @Value("${payu.merchant-salt}")
    private String merchantSalt;

    @Value("${payu.post-service-url:https://test.payu.in/merchant/postservice?form=2}")
    private String payuPostServiceUrl;

    //In case webhooks fail or are delayed, run a periodic Spring @Scheduled cron job to query PayU's status API for pending refunds.
    // Runs every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void syncPendingRefunds() {
        List<OrderRefund> pendingRefunds = refundRepository.findByRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);

        for (OrderRefund refund : pendingRefunds) {
            checkAndSyncRefundStatus(refund);
        }
    }

    private void checkAndSyncRefundStatus(OrderRefund refund) {
        String command = "check_action_status";
        String requestId = refund.getGatewayRefundId(); // PayU request_id

        // Hash: key|command|var1|salt
        String hashSequence = String.format("%s|%s|%s|%s", payUMerchantKey, command, requestId, merchantSalt);
        String hash = hashSha512(hashSequence);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", payUMerchantKey);
        body.add("command", command);
        body.add("var1", requestId);
        body.add("hash", hash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(payuPostServiceUrl, requestEntity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root.path("status").asInt() == 1) {
                // Navigate into transaction_details -> requestId -> requestId node
                JsonNode detailsNode = root.path("transaction_details").path(requestId).path(requestId);

                if (!detailsNode.isMissingNode()) {
                    String refundStatus = detailsNode.path("status").asText();
                    String bankRefNum = detailsNode.path("bank_ref_num").asText(null);
                    String mode = detailsNode.path("mode").asText(null);
                    String bankCode = detailsNode.path("bankcode").asText(null);

                    if ("success".equalsIgnoreCase(refundStatus)) {
                        // Update Refund Entity
                        refund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                        refund.setBankArn(bankRefNum);      // Save bank_ref_num
                        refund.setUpdatedAt(LocalDateTime.now());

                        refundRepository.save(refund);

                        Optional<PaymentTransaction> paymentTransactionOptional = transactionRepository.findByApplicationOrderId(refund.getApplicationOrderId());
                        PaymentTransaction paymentTransaction = paymentTransactionOptional.get();

                        paymentTransaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                        paymentTransaction.setUpdatedAt(LocalDateTime.now());

                        transactionRepository.save(paymentTransaction);

                        log.info("Successfully updated refund status for ID: {}", refund.getRefundTransactionsId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error verifying refund status for refund ID: {}", refund.getRefundTransactionsId(), e);
        }
    }


    private String hashSha512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing SHA-512 hash", e);
        }
    }
}


