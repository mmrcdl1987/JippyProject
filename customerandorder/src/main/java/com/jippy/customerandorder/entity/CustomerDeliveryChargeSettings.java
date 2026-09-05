package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_delivery_charge_settings", schema = "jippy_customer_and_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeliveryChargeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_delivery_charge_settings_id")
    private Integer customerDeliveryChargeSettingsId;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "plan_name", nullable = false, length = 30)
    private String planName;

    @Column(name = "order_value_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderValueThreshold;

    @Column(name = "free_distance_kms", nullable = false, precision = 10, scale = 2)
    private BigDecimal freeDistanceKms;

    @Column(name = "charge_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargePerKm;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}