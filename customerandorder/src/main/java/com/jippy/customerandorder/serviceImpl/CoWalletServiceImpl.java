package com.jippy.customerandorder.serviceImpl;


import com.jippy.customerandorder.dto.CoCustomerWalletResponseDto;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.iservice.CoWalletService;
import com.jippy.customerandorder.repository.CoCustomerWalletRepository;
import com.jippy.customerandorder.repository.CoCustomerWalletTransactionsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CoWalletServiceImpl implements CoWalletService {

    private final CoCustomerWalletRepository walletRepository;
    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    @Override
     public CoCustomerWalletResponseDto getByCustomerId(Integer customerId) {

        CoCustomerWallet wallet = walletRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException(
                                "Wallet not found for customer id: " + customerId));

        CoCustomerWalletResponseDto response = new CoCustomerWalletResponseDto();

        response.setWalletId(wallet.getWalletId());
        CoCustomer customer = wallet.getCustomer();
        response.setCustomerId(customer.getCustomerId());
        response.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        response.setBalanceAmount(wallet.getBalanceAmount());
        response.setBalancePoints(wallet.getBalancePoints());

        return response;
    }


    @Override
    @Transactional
    public CoCustomerWallet updateByCustomerId(
            Integer customerId,
            CoCustomerWallet walletDetails) {

        CoCustomerWallet existingWallet =
                walletRepository.findByCustomerCustomerId(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Wallet not found for customer id: "
                                                + customerId
                                )
                        );

        int oldPoints = existingWallet.getBalancePoints() != null
                ? existingWallet.getBalancePoints()
                : 0;

        int newPoints = walletDetails.getBalancePoints() != null
                ? walletDetails.getBalancePoints()
                : 0;

        BigDecimal oldAmount = existingWallet.getBalanceAmount() != null
                ? existingWallet.getBalanceAmount()
                : BigDecimal.ZERO;

        BigDecimal newAmount = walletDetails.getBalanceAmount() != null
                ? walletDetails.getBalanceAmount()
                : BigDecimal.ZERO;

        // Calculate differences
        int pointsDifference = newPoints - oldPoints;
        BigDecimal amountDifference = newAmount.subtract(oldAmount);

        // Update wallet
        existingWallet.setBalancePoints(newPoints);
        existingWallet.setBalanceAmount(newAmount);
        existingWallet.setUpdatedBy(walletDetails.getUpdatedBy());
        existingWallet.setUpdatedAt(LocalDateTime.now());

        CoCustomerWallet updatedWallet =
                walletRepository.save(existingWallet);

        // Create transaction if points or amount changed
        if (pointsDifference != 0 || amountDifference.compareTo(BigDecimal.ZERO) != 0) {

            CoCustomerWalletTransactions transaction =
                    new CoCustomerWalletTransactions();

            transaction.setWalletId(existingWallet.getWalletId());

            // Store absolute values
            if (pointsDifference != 0) {
                transaction.setPoints(Math.abs(pointsDifference));
            }

            if (amountDifference.compareTo(BigDecimal.ZERO) != 0) {
                transaction.setAmount(amountDifference.abs());
            }

            // Determine transaction type
            if (pointsDifference > 0
                    || amountDifference.compareTo(BigDecimal.ZERO) > 0) {

                transaction.setTransactionType("CREDIT");

            } else {

                transaction.setTransactionType("DEBIT");
            }

            transaction.setCreatedBy(walletDetails.getCreatedBy());
            transaction.setUpdatedBy(walletDetails.getUpdatedBy());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());

            transactionsRepository.save(transaction);
        }

        return updatedWallet;
    }
}