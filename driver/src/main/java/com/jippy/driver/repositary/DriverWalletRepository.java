package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverWalletRepository extends JpaRepository<DriverWallet, Integer> {

//    used for fleet manager to update COD amount for driver and also
//    activate the driver if the updated COD amount is greater than 0
    Optional<DriverWallet> findByDriverId(Integer driverId);
}