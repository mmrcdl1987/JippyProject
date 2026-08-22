package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoWalletTransactionHistoryDto {

    private String transactionType;
    private Integer points;
    private BigDecimal amount;
    private LocalDateTime createdAt;

}