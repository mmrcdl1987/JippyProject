package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.iservice.CoWalletTransactionsService;
import com.jippy.customerandorder.repository.CoCustomerWalletRepository;
import com.jippy.customerandorder.repository.CoCustomerWalletTransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoWalletTransactionsServiceImpl implements CoWalletTransactionsService {

    private final CoCustomerWalletRepository walletRepository;
    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    @Override
    public List<CoCustomerWalletTransactions> getTransactionsByCustomerId(
            Integer customerId
    ) {

        return walletRepository
                .findByCustomerCustomerId(customerId)
                .map(wallet ->
                        transactionsRepository
                                .findByWalletIdOrderByCreatedAtDesc(
                                        wallet.getWalletId()
                                )
                )
                .orElse(Collections.emptyList());
    }

    @Override
    public List<CoCustomerWalletTransactions> getAllTransactions() {

        return transactionsRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

}
