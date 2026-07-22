package com.jippy.division.controller;

import com.jippy.division.dto.DivPaymentInitiateResponse;
import com.jippy.division.dto.DivPlaceOrderRequestDto;
import com.jippy.division.dto.PaymentVerifyRequestDto;
import com.jippy.division.service.DivPaymentService;
import com.paytm.pg.merchant.PaytmChecksum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/div/payment")
@AllArgsConstructor
@Slf4j
public class DivPaymentController {

    private final DivPaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<DivPaymentInitiateResponse> initiate(@Valid @RequestBody
    DivPlaceOrderRequestDto placeOrderRequestDto) throws Exception {
        try{
            log.info("Initiating payment for orderId: {}", placeOrderRequestDto.getOrderId());
            return ResponseEntity.ok(paymentService.initiatePayment(placeOrderRequestDto));
        }catch (Exception e){
            throw new Exception("Error initiating payment: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody PaymentVerifyRequestDto request) {
        log.info("Verifying payment for orderId: {}, paymentId: {}", request.getApplicationOrderId(),
                request.getRzpPaymentId());
        boolean isValid = paymentService.verifyPaymentSignature(request);
        if (isValid) {
            return ResponseEntity.ok("Payment verified successfully. Order confirmed.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payment signature signature mismatch.");
    }

    @PostMapping("/orderRefund")
    public ResponseEntity<String> orderRefund(@RequestParam String orderId,
           @RequestParam String reason) {
        try{
            log.info("Processing refund for orderId: {}, reason: {}", orderId, reason);

            String response = paymentService.orderRefund(orderId, reason);
            return ResponseEntity.status(HttpStatus.OK).body("Refund request processed successfully. Refund ID: " + response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing refund: " + e.getMessage());
        }
    }

    @PostMapping("/callback/{orderId}")
    public ResponseEntity<String> paytmPaymentCallback(@PathVariable String orderId, HttpServletRequest request) {

        log.info("Received payment callback for orderId: {}", orderId);
        return paymentService.paytmPaymentCallback(orderId, request);
    }


}
