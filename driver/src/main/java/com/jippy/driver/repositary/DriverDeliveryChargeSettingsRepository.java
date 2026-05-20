package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface DriverDeliveryChargeSettingsRepository extends JpaRepository<DriverDeliveryChargeSettings, Integer> {

    @Query("""
            SELECT d
            FROM DriverDeliveryChargeSettings d
            WHERE :pickupDistance
            BETWEEN d.pickUpKmsRangeFrom
            AND d.pickUpKmsRangeTo
            """)
    Optional<DriverDeliveryChargeSettings> findPickupSlab(@Param("pickupDistance") BigDecimal pickupDistance);

    @Query("""
            SELECT d
            FROM DriverDeliveryChargeSettings d
            WHERE :deliveryDistance
            BETWEEN d.deliveryKmsRangeFrom
            AND d.deliveryKmsRangeTo
            """)
    Optional<DriverDeliveryChargeSettings> findDeliverySlab(@Param("deliveryDistance") BigDecimal deliveryDistance);

}
