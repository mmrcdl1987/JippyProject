package com.jippy.division.controller;

import com.jippy.division.service.DivOrderRefundService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/div/payment/refund")
@AllArgsConstructor
@Slf4j
public class DivOrderRefundController {

    private final DivOrderRefundService orderRefundService;

    @PostMapping("/orderRefund")
    public ResponseEntity<String> orderRefund(@RequestParam String orderId,
            @RequestParam String reason) {
        try{
            log.info("Processing refund for orderId: {}, reason: {}", orderId, reason);

            String response = orderRefundService.orderRefund(orderId, reason);
            return ResponseEntity.status(HttpStatus.OK).body("Refund request processed successfully. Refund ID: " + response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing refund: " + e.getMessage());
        }
    }
}
