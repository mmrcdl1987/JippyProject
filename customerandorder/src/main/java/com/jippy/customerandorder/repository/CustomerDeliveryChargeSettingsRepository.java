package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CustomerDeliveryChargeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDeliveryChargeSettingsRepository
        extends JpaRepository<CustomerDeliveryChargeSettings, Integer> {

    List<CustomerDeliveryChargeSettings> findByCityIdOrderByOrderValueThresholdAsc(
            Integer cityId
    );

    List<CustomerDeliveryChargeSettings> findByCityIdAndIsActiveTrueOrderByOrderValueThresholdAsc(
            Integer cityId
    );

    Optional<CustomerDeliveryChargeSettings>
    findFirstByCityIdAndIsActiveTrueAndOrderValueThresholdLessThanEqualOrderByOrderValueThresholdDesc(
            Integer cityId,
            BigDecimal orderValue
    );

    boolean existsByCityIdAndOrderValueThreshold(
            Integer cityId,
            BigDecimal orderValueThreshold
    );

    boolean existsByCityIdAndOrderValueThresholdAndCustomerDeliveryChargeSettingsIdNot(
            Integer cityId,
            BigDecimal orderValueThreshold,
            Integer id
    );

    @Query("""
        SELECT c
        FROM CustomerDeliveryChargeSettings c
        WHERE c.cityId = :cityId
          AND c.isActive = true
          AND c.orderValueThreshold <= :orderAmount
        ORDER BY c.orderValueThreshold DESC
        """)
    List<CustomerDeliveryChargeSettings> findApplicablePlans(
            @Param("cityId") Integer cityId,
            @Param("orderAmount") BigDecimal orderAmount
    );
}