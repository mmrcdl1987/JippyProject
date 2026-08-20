package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoCustomerWalletTransactionsRepository extends JpaRepository<CoCustomerWalletTransactions, Integer> {

    List<CoCustomerWalletTransactions> findByWalletIdOrderByCreatedAtDesc(Integer walletId);


    boolean existsByOrderIdAndPointsType(String orderId, String pointsType);
}