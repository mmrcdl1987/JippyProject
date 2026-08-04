package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverWalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverWalletTransactionsRepository extends
        JpaRepository<DriverWalletTransactions, Integer> {

    List<DriverWalletTransactions> findByDriverWalletId(
            Integer driverWalletId);
    /**
     * Check whether COD deduction already exists
     * for the given order.
     */
    boolean existsByOrderId(String orderId);

}
