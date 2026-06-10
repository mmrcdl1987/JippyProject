package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.DriverWalletTransactionResponseDto;
import com.jippy.driver.entity.DriverWallet;
import com.jippy.driver.entity.DriverWalletTransactions;
import com.jippy.driver.exception.ResourceNotFoundException;
import com.jippy.driver.mapper.DriverWalletTransactionMapper;
import com.jippy.driver.repositary.DriverWalletRepository;
import com.jippy.driver.repositary.DriverWalletTransactionsRepository;
import com.jippy.driver.service.DriverWalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverWalletTransactionServiceImpl implements DriverWalletTransactionService {

    private final DriverWalletRepository driverWalletRepository;

    private final DriverWalletTransactionsRepository transactionRepository;

    @Override
    public List<DriverWalletTransactionResponseDto> getDriverWalletTransactions(Integer driverId) {

        log.info("Fetching wallet transactions for driverId : {}", driverId);

        Optional<DriverWallet> optionalWallet = driverWalletRepository.findByDriverId(driverId);

        if (optionalWallet.isEmpty()) {

            throw new ResourceNotFoundException("Driver wallet not found for driverId : " + driverId);
        }

        DriverWallet wallet = optionalWallet.get();

        log.info("Driver wallet found. WalletId : {}", wallet.getDriverWalletId());

        List<DriverWalletTransactions> transactions = transactionRepository.findByDriverWalletId(wallet.getDriverWalletId());

        if (transactions.isEmpty()) {

            throw new ResourceNotFoundException("No wallet transactions found for driverId : " + driverId);
        }

        List<DriverWalletTransactionResponseDto> responseList = new ArrayList<>();

        for (DriverWalletTransactions transaction : transactions) {

            responseList.add(DriverWalletTransactionMapper.toResponseDto(transaction));
        }

        log.info("Total transactions fetched : {}", responseList.size());

        return responseList;
    }
}