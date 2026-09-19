package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DivPaymentInitiateResponse {

    private String orderId;
    private String razorpayOrderId;
    private BigDecimal toPayAmount;
    private String paytmTxnToken;
    private String payUHash;
    private Map<String, String> payUParams;
    private String payuUrl;
}
