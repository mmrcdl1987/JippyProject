package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.DriverWalletUpdateResponseDto;
import com.jippy.foodandmart.dto.FmUpdateCODResponseDto;

public class FmFleetManagerMapper {

    public static FmUpdateCODResponseDto toUpdateCODResponseDto(DriverWalletUpdateResponseDto walletResponse, String isActive) {

        FmUpdateCODResponseDto response = new FmUpdateCODResponseDto();

        response.setDriverId(walletResponse.getDriverId());

        response.setPreviousCodAmount(walletResponse.getPreviousCodAmount());

        response.setUpdatedCodAmount(walletResponse.getUpdatedCodAmount());

        response.setOrdersLock(walletResponse.getOrdersLock());

        response.setIsActive(isActive);

        response.setAmountToPay(walletResponse.getAmountToPay());

        response.setTotalTransactionsUpdated(walletResponse.getTotalTransactionsUpdated());

        response.setTransactionStatus(walletResponse.getTransactionStatus());

        response.setMessage(walletResponse.getMessage());

        return response;
    }
}