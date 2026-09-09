package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DivRefundDetailsDto {

    private String applicationOrderId;

    private String paymentTransactionsId;

    private BigDecimal amountInRupees;

    private String refundStatus;

    private String reason;

    private LocalDateTime createdAt;
}