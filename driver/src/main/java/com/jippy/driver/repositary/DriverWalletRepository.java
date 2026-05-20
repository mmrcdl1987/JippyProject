package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverWalletRepository extends JpaRepository<DriverWallet, Integer> {

    Optional<DriverWallet> findByDriverId(Integer driverId);
}