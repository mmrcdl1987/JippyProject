package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverMsDeliveryChargeSettingsRepository
        extends JpaRepository<DriverDeliveryChargeSettings, Integer> {

}
