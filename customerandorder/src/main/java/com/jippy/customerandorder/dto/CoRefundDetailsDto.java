package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoRefundDetailsDto {

    private String applicationOrderId;

    private String paymentTransactionsId;

    /**
     * Refund amount converted from paise to rupees.
     *
     * Example:
     * 45443 paise = 454.43 rupees
     */
    private BigDecimal amountInRupees;

    private String refundStatus;

    private String reason;

    private LocalDateTime createdAt;
}