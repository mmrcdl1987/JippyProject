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

    List<CustomerDeliveryChargeSettings> findByAreaIdOrderByOrderValueThresholdAsc(
            Integer areaId
    );

    List<CustomerDeliveryChargeSettings> findByAreaIdAndIsActiveTrueOrderByOrderValueThresholdAsc(
            Integer areaId
    );

    Optional<CustomerDeliveryChargeSettings>
    findFirstByAreaIdAndIsActiveTrueAndOrderValueThresholdLessThanEqualOrderByOrderValueThresholdDesc(
            Integer areaId,
            BigDecimal orderValue
    );

    boolean existsByAreaIdAndOrderValueThreshold(
            Integer areaId,
            BigDecimal orderValueThreshold
    );

    boolean existsByAreaIdAndOrderValueThresholdAndCustomerDeliveryChargeSettingsIdNot(
            Integer areaId,
            BigDecimal orderValueThreshold,
            Integer id
    );

    @Query("""
        SELECT c
        FROM CustomerDeliveryChargeSettings c
        WHERE c.areaId = :areaId
          AND c.isActive = true
          AND c.orderValueThreshold <= :orderAmount
        ORDER BY c.orderValueThreshold DESC
        """)
    List<CustomerDeliveryChargeSettings> findApplicablePlans(
            @Param("areaId") Integer areaId,
            @Param("orderAmount") BigDecimal orderAmount
    );
}