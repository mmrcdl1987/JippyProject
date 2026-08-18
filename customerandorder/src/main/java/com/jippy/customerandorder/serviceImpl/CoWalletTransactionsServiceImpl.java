package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.iservice.CoWalletTransactionsService;
import com.jippy.customerandorder.repository.CoCustomerWalletRepository;
import com.jippy.customerandorder.repository.CoCustomerWalletTransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoWalletTransactionsServiceImpl implements CoWalletTransactionsService {

    private final CoCustomerWalletRepository walletRepository;
    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    @Override
    public List<CoCustomerWalletTransactions> getTransactionsByCustomerId(
            Integer customerId
    ) {

        CoCustomerWallet wallet = walletRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found for customer id: " + customerId
                        )
                );

        return transactionsRepository
                .findByWalletIdOrderByCreatedAtDesc(
                        wallet.getWalletId()
                );
    }

    @Override
    public List<CoCustomerWalletTransactions> getAllTransactions(){
        return transactionsRepository.findAll(
                Sort.by(Sort.Direction.DESC,"createdAt")
        );
    }

}
