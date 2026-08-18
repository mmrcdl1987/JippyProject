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

        Integer oldPoints = existingWallet.getBalancePoints();
        Integer newPoints = walletDetails.getBalancePoints();

        // Update wallet
        existingWallet.setBalanceAmount(walletDetails.getBalanceAmount());
        existingWallet.setBalancePoints(newPoints);
        existingWallet.setUpdatedBy(walletDetails.getUpdatedBy());
        existingWallet.setUpdatedAt(LocalDateTime.now());

        CoCustomerWallet updatedWallet = walletRepository.save(existingWallet);

        // Calculate points difference
        int pointsDifference = newPoints - oldPoints;

        // Create transaction only if points changed
        if (pointsDifference != 0) {

            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
            transaction.setWalletId(existingWallet.getWalletId());

            if (pointsDifference > 0) {
                transaction.setPointsType("CREDIT");
                transaction.setPoints(pointsDifference);
            } else {
                transaction.setPointsType("DEBIT");
                transaction.setPoints(Math.abs(pointsDifference));
            }
            transaction.setCreatedBy(walletDetails.getUpdatedBy());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction.setUpdatedBy(walletDetails.getUpdatedBy());

            transactionsRepository.save(transaction);
        }

        return updatedWallet;
    }
}