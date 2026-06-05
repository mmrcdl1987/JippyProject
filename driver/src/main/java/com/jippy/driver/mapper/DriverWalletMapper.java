package com.jippy.driver.mapper;

import com.jippy.driver.dto.DriverWalletUpdateResponseDto;

import java.math.BigDecimal;

public class DriverWalletMapper {

    public static DriverWalletUpdateResponseDto
    toDriverWalletUpdateResponseDto(
            Integer driverId,
            BigDecimal previousCodAmount,
            BigDecimal updatedCodAmount,
            Boolean ordersLock,
            BigDecimal amountToPay,
            Integer totalTransactionsUpdated,
            String transactionStatus,
            String message) {

        DriverWalletUpdateResponseDto response =
                new DriverWalletUpdateResponseDto();

        response.setDriverId(driverId);
        response.setPreviousCodAmount(previousCodAmount);
        response.setUpdatedCodAmount(updatedCodAmount);
        response.setOrdersLock(ordersLock);
        response.setAmountToPay(amountToPay);
        response.setTotalTransactionsUpdated(totalTransactionsUpdated);
        response.setTransactionStatus(transactionStatus);
        response.setMessage(message);

        return response;
    }
}