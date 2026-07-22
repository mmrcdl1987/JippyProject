package com.jippy.division.service;

import org.springframework.http.ResponseEntity;

public interface RazorPayWebhookService {

    ResponseEntity<?> handleRazorpayWebhook(String payload, String signature);
}
