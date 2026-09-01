package com.jippy.division.service;

import com.jippy.division.dto.PaymentHashRequestDto;

import java.util.Map;

public interface PayUService {

    Map<String, String> generatePaymentHash(PaymentHashRequestDto hashRequest);
    boolean verifyResponseHash(Map<String, String> payuResponseParams);

    public boolean initiateRefund(String payuPaymentId, String refundTransactionId, Integer amountInPaise);
}
