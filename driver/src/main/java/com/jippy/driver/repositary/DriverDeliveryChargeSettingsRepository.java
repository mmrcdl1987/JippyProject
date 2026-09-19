package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface DriverDeliveryChargeSettingsRepository
        extends JpaRepository<DriverDeliveryChargeSettings, Integer> {
    // PICKUP SLAB

    @Query("""
            SELECT d
            FROM DriverDeliveryChargeSettings d
            WHERE :pickupDistance >= d.kmsRangeFrom
              AND :pickupDistance < d.kmsRangeTo
              AND UPPER(d.chargeType) = 'PICKUP'
            """)
    Optional<DriverDeliveryChargeSettings> findPickupSlab(
            @Param("pickupDistance") BigDecimal pickupDistance
    );


// DELIVERY SLAB
@Query("""
    SELECT d
    FROM DriverDeliveryChargeSettings d
    WHERE :deliveryDistance >= d.kmsRangeFrom
      AND :deliveryDistance < d.kmsRangeTo
      AND UPPER(d.deliveryType) = 'DELIVERY'
      AND UPPER(d.status) = 'ACTIVE'
    ORDER BY d.kmsRangeFrom
    """)
Optional<DriverDeliveryChargeSettings> findDeliverySlab(
        @Param("deliveryDistance") BigDecimal deliveryDistance
);


// NEW: ZONE-BASED DELIVERY SLAB

    @Query(value = """
        SELECT d.*
        FROM jippy_driver.driver_delivery_charge_settings d
        WHERE d.zone_id = :zoneId
          AND d.kms_range_from <= :deliveryDistance
          AND d.kms_range_to > :deliveryDistance
          AND UPPER(d.delivery_type) = 'DELIVERY'
          AND UPPER(d.status) = 'Y'
        ORDER BY d.kms_range_from ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<DriverDeliveryChargeSettings> findDeliverySlabByZone(
            @Param("zoneId") Integer zoneId,
            @Param("deliveryDistance") BigDecimal deliveryDistance
    );
    // GET DELIVERY CHARGE SETTINGS
    // SERVER-SIDE PAGINATION

    @Override
    Page<DriverDeliveryChargeSettings> findAll(
            Pageable pageable
    );
}