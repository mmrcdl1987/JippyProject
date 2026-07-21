package com.jippy.division.controller;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.RazorPayWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/div/webhooks")
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookController {

  private final RazorPayWebhookService razorPayWebhookService;

    @PostMapping("/razorpay")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Received Razorpay webhook: payload={}, signature={}", payload, signature);
        razorPayWebhookService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok().build(); // Return 200 OK to stop Razorpay from retrying the webhook
    }
}
