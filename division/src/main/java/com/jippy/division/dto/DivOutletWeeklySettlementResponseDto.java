package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DivOutletWeeklySettlementResponseDto {

    private Integer weeklySettlementId;

    private Integer outletId;

    private LocalDate weekStartDate;

    private LocalDate weekEndDate;

    private BigDecimal totalSettlementAmount;

    private BigDecimal deductions;

    private BigDecimal gst;

    private BigDecimal promotionAmount;

    private BigDecimal subscriptionAmount;

    private BigDecimal netSettlementAmount;

    private String paymentStatus;

    private String transactionId;

    private String outletMobileNumber;

    private String outletEmail;

    private Integer ordersCount;
}