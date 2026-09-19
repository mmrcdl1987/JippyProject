package com.jippy.division.dto;

import lombok.Data;

@Data
public class DivOrderPaymentStatusDto {

    private String orderId;
    private String message;
    private String transactionId;
    private String paymentStatus;
}
