package com.jippy.driver.mapper;

import com.jippy.driver.dto.DriverWalletTransactionResponseDto;
import com.jippy.driver.entity.DriverWalletTransactions;

public class DriverWalletTransactionMapper {

    public static DriverWalletTransactionResponseDto toResponseDto(DriverWalletTransactions transaction) {

        DriverWalletTransactionResponseDto dto = new DriverWalletTransactionResponseDto();

        dto.setTransactionId(transaction.getDriverWalletTransactionsId());

        dto.setDriverWalletId(transaction.getDriverWalletId());

        dto.setOrderId(transaction.getOrderId());

        dto.setCodAmount(transaction.getCodAmount());

        dto.setTransactionType(transaction.getTransactionType());

        dto.setCreatedAt(transaction.getCreatedAt());

        dto.setCreatedBy(transaction.getCreatedBy());

        dto.setUpdatedAt(transaction.getUpdatedAt());

        dto.setUpdatedBy(transaction.getUpdatedBy());

        return dto;
    }
}