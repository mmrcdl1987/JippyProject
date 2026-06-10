package com.jippy.driver.service;

import com.jippy.driver.dto.DriverWalletTransactionResponseDto;

import java.util.List;

public interface DriverWalletTransactionService {

    List<DriverWalletTransactionResponseDto> getDriverWalletTransactions(Integer driverId);
}
