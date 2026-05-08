package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_delivery_charge_settings", schema = "jippy_customer_and_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverDeliveryChargeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_charge_setting_id")
    private Integer deliveryChargeSettingId;

    @Column(name = "pick_up_kms_range_from", nullable = false, precision = 10, scale = 2)
    private BigDecimal pickUpKmsRangeFrom;

    @Column(name = "pick_up_kms_range_to", nullable = false, precision = 10, scale = 2)
    private BigDecimal pickUpKmsRangeTo;

    @Column(name = "unit_price_per_pick_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPricePerPickKm;

    @Column(name = "delivery_kms_range_from", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryKmsRangeFrom;

    @Column(name = "delivery_kms_range_to", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryKmsRangeTo;

    @Column(name = "unit_price_per_deliver_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPricePerDeliverKm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}