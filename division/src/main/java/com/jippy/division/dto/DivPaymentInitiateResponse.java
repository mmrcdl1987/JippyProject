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
    private Map<String, String> payload;
    private String payment_url;
    private String message;
    private Boolean success;
}
