/*
package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_incentive_settings", schema = "jippy_customer_and_order")
@Getter
@Setter
public class CoDriverIncentiveSettings{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverIncentiveSettingsId;

    @Column(nullable = false)
    private Integer ordersCount;

    @Column(nullable = false)
    private BigDecimal incentiveAmount;

    private LocalDateTime createdAt;
    private Integer createdBy;

    private LocalDateTime updatedAt;
    private Integer updatedBy;

}*/
