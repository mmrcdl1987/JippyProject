package com.jippy.foodandmart.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmUpdateCODResponseDto {

    private Integer driverId;

    private BigDecimal previousCodAmount;

    private BigDecimal updatedCodAmount;

    private Boolean ordersLock;

    private String isActive;

    private BigDecimal amountToPay;

    private Integer totalTransactionsUpdated;

    private String transactionStatus;

    private String message;
}