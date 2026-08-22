package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_incentive_settings", schema = "jippy_driver")
@Getter
@Setter
public class DriverIncentiveSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverIncentiveSettingsId;

    @Column(nullable = false)
    private Integer ordersCount;

    @Column(nullable = false)
    private BigDecimal incentiveAmount;

    @Column(name = "zone_id")
    private Integer zoneId;

    private LocalDateTime createdAt;
    private Integer createdBy;

    private LocalDateTime updatedAt;
    private Integer updatedBy;

}