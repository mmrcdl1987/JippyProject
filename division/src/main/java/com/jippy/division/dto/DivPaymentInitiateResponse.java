package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DivPaymentInitiateResponse {

    private String orderId;
    private String razorpayOrderId;
    private BigDecimal toPayAmount;
    private String paytmTxnToken;
    private String payUHash;
    private String payUMerchantKey;

}
