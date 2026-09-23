package com.jippy.division.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.dto.PaymentHashRequestDto;
import com.jippy.division.service.PayUService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayUServiceImpl implements PayUService {

    @Value("${payu.merchant-key}")
    private String merchantKey;

    @Value("${payu.merchant-salt}")
    private String merchantSalt;

    @Value("${payu.refund-url}")
    private String refundUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, String> generatePaymentHash(PaymentHashRequestDto request) {
        // Ensure amount is strictly formatted to 2 decimal places
        String formattedAmount = String.format(Locale.US, "%.2f",
                Double.parseDouble(request.getAmount().toString()));

        String firstname = request.getCustomerName() != null ? request.getCustomerName().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String productinfo = request.getProductinfo() != null ? request.getProductinfo().trim() : "";
        String txnid = request.getTxnid() != null ? request.getTxnid().trim() : "";

        // 10 empty string elements for udf1 through udf10
        // String.join inserts 11 pipes between email and salt automatically!
        String hashSequence = String.join("|",
                merchantKey, txnid, formattedAmount, productinfo, firstname, email,
                "", "", "", "", "", "", "", "", "", "",
                merchantSalt
        );

        System.out.println("================"+hashSequence);

        // Ensure hash is strictly lower-case
        String paymentHash = hashSha512(hashSequence).toLowerCase();

        System.out.println("================"+paymentHash);

        Map<String, String> result = new HashMap<>();
        result.put("paymentHash", paymentHash);
        result.put("merchantKey", merchantKey);
        return result;
    }

    @Override
    public boolean verifyResponseHash(Map<String, String> payuParams) {
        String receivedHash = payuParams.get("hash");
        if (receivedHash == null) {
            return false;
        }

        String status = payuParams.getOrDefault("status", "");
        String email = payuParams.getOrDefault("email", "");
        String firstname = payuParams.getOrDefault("firstname", "");
        String productinfo = payuParams.getOrDefault("productinfo", "");
        String amount = payuParams.getOrDefault("amount", "");
        String txnid = payuParams.getOrDefault("txnid", "");
        String key = payuParams.getOrDefault("key", merchantKey).trim();

        // Reverse Hash Sequence:
        // SALT|status|||||||||||email|firstname|productinfo|amount|txnid|key
        // String.join inserts exactly 11 pipes between status and email for udf10 through udf1
        String hashSequence = String.join("|",
                merchantSalt.trim(),
                status,
                "", "", "", "", "", "", "", "", "", "", // 10 empty slots for udf10..udf1
                email,
                firstname,
                productinfo,
                amount,
                txnid,
                key
        );

        // If additionalCharges exist (e.g., convenience fees), PayU prepends it to the reverse hash
        if (payuParams.containsKey("additionalCharges")) {
            hashSequence = payuParams.get("additionalCharges") + "|" + hashSequence;
        }

        String calculatedHash = hashSha512(hashSequence);

        System.out.println("====== Calculated Hash: " + calculatedHash);
        System.out.println("====== Received Hash:   " + receivedHash);

        return calculatedHash.equalsIgnoreCase(receivedHash);
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

    public boolean initiateRefund(String payuPaymentId, String refundTransactionId, Integer amountInPaise) {
        String command = "cancel_refund_transaction";

        // Convert paise to Rupees string formatted to 2 decimal places (e.g., 15000 -> "150.00")
        String amountStr = String.format("%.2f", amountInPaise / 100.0);

        // 1. Hash Sequence: key|command|var1|SALT (var1 is mihpayid)
        String hashSequence = String.format("%s|%s|%s|%s", merchantKey, command, payuPaymentId, merchantSalt);
        String hash = hashSha512(hashSequence);

        // 2. Prepare Form Data (PayU API expects application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", merchantKey);
        body.add("command", command);
        body.add("hash", hash);
        body.add("var1", payuPaymentId);             // mihpayid from original payment
        body.add("var2", refundTransactionId);        // Unique Request ID generated by your DB
        body.add("var3", amountStr);                  // Refund amount

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Initiating PayU refund for PayUID: {} | RefundTxnID: {} | Amount: {}", payuPaymentId, refundTransactionId, amountStr);
            ResponseEntity<String> response = restTemplate.exchange(refundUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {

                String responseBody = response.getBody().trim();

                // 1. Check if response is HTML or non-JSON
                if (responseBody.startsWith("<")) {
                    log.error("PayU returned HTML response instead of JSON. Raw Response: {}", responseBody);
                    return false;
                }

                // 2. Validate JSON structure before parsing
                if (!responseBody.startsWith("{") && !responseBody.startsWith("[")) {
                    log.error("PayU returned non-JSON body: {}", responseBody);
                    return false;
                }


                JsonNode root = objectMapper.readTree(response.getBody());
                int status = root.path("status").asInt(-1);
                String msg = root.path("msg").asText();

                if (status == 1) {
                    log.info("PayU Refund initiated successfully: {}", msg);
                    return true;
                } else {
                    log.error("PayU Refund failed: {}", msg);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred while calling PayU refund API", e);
        }
        return false;
    }
}
