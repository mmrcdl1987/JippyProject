package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DriverWalletTransactionResponseDto {

    private Integer transactionId;

    private Integer driverWalletId;

    private String orderId;

    private BigDecimal codAmount;

    private String transactionType;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}