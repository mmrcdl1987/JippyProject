package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoCustomerWalletTransactionsRepository extends JpaRepository<CoCustomerWalletTransactions, Integer> {
}