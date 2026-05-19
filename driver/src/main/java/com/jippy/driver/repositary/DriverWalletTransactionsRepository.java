package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverWalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverWalletTransactionsRepository extends
        JpaRepository<DriverWalletTransactions, Integer> {

}
