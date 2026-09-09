package com.jippy.division.controller;

import com.jippy.division.service.DivPaymentService;
import com.jippy.division.service.DivPayuWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/div/payments")
@RequiredArgsConstructor
public class PayuWebhookController {

    private final DivPayuWebhookService payuWebhookService;

    /**
     * Webhook/S2S (Server-to-Server) Endpoint configured in PayU Dashboard
     */
    @PostMapping(value = "/webhook/payu", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handlePayUWebhook(@RequestParam Map<String, String> payuParams) {
        try {
            boolean isProcessed = payuWebhookService.processPayUWebhook(payuParams);

            if (isProcessed) {
                // Return 200 OK so PayU acknowledges delivery and stops retrying
                return ResponseEntity.ok("Payu Webhook Processed Successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Signature/Hash Mismatch");
            }
        } catch (Exception e) {
            // Log error internally and return HTTP 500 so PayU can retry later
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing Error");
        }
    }

    @PostMapping(value = "/webhook/payu-refund", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handlePayURefundWebhook(@RequestParam Map<String, String> params) {
        boolean processed = payuWebhookService.processRefundWebhook(params);
        return processed ? ResponseEntity.ok("OK") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FAILED");
    }
}
