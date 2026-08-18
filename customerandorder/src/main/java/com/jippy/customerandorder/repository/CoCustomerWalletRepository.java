package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CoCustomerWalletRepository extends JpaRepository<CoCustomerWallet, Integer> {

    @Query("""
        SELECT w
        FROM CoCustomerWallet w
        JOIN FETCH w.customer c
        WHERE c.customerId = :customerId
    """)
    Optional<CoCustomerWallet> findByCustomerCustomerId(Integer customerId);
}