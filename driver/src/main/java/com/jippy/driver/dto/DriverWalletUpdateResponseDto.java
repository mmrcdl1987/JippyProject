package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverWalletUpdateResponseDto {

    private Integer driverId;

    private BigDecimal previousCodAmount;

    private BigDecimal updatedCodAmount;

    private Boolean ordersLock;

    private BigDecimal amountToPay;

    private Integer totalTransactionsUpdated;

    private String transactionStatus;

    private String message;
}