package com.jippy.division.service;

import com.jippy.division.dto.DivPaymentInitiateResponse;
import com.jippy.division.dto.DivPlaceOrderRequestDto;
import com.jippy.division.dto.PaymentVerifyRequestDto;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface DivPaymentService {
    DivPaymentInitiateResponse initiatePayment(DivPlaceOrderRequestDto placeOrderRequestDto)
            throws RazorpayException;

    boolean verifyAndCompletePayment(PaymentVerifyRequestDto request);

    ResponseEntity<String> paytmPaymentCallback(String orderId, HttpServletRequest request);
}
