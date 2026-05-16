package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoCustomerWalletRepository extends JpaRepository<CoCustomerWallet, Integer> {

    Optional<CoCustomerWallet> findByCustomerCustomerId(Integer customerId);
}