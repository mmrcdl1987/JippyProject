package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoWalletResponseDto {

    private Boolean success;

    private String message;

    private Integer walletId;

    private Integer customerId;

    private Integer balancePoints;

    private BigDecimal balanceAmount;

    //private Integer returnedPoints;
}