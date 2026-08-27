package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "driver_delivery_charge_settings",
        schema = "jippy_driver"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//public class DriverDeliveryChargeSettings {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "delivery_charge_setting_id")
//    private Integer deliveryChargeSettingId;
//
//    @Column(name = "pick_up_kms_range_from", nullable = false, precision = 10, scale = 2)
//    private BigDecimal pickUpKmsRangeFrom;
//
//    @Column(name = "pick_up_kms_range_to", nullable = false, precision = 10, scale = 2)
//    private BigDecimal pickUpKmsRangeTo;
//
//    @Column(name = "unit_price_per_pick_km", nullable = false, precision = 10, scale = 2)
//    private BigDecimal unitPricePerPickKm;
//
//    @Column(name = "delivery_kms_range_from", nullable = false, precision = 10, scale = 2)
//    private BigDecimal deliveryKmsRangeFrom;
//
//    @Column(name = "delivery_kms_range_to", nullable = false, precision = 10, scale = 2)
//    private BigDecimal deliveryKmsRangeTo;
//
//    @Column(name = "unit_price_per_deliver_km", nullable = false, precision = 10, scale = 2)
//    private BigDecimal unitPricePerDeliverKm;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "created_by")
//    private Integer createdBy;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @Column(name = "updated_by")
//    private Integer updatedBy;
//}
public class DriverDeliveryChargeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_charge_setting_id")
    private Integer deliveryChargeSettingId;

    @Column(name = "kms_range_from", nullable = false, precision = 10, scale = 2)
    private BigDecimal kmsRangeFrom;

    @Column(name = "kms_range_to", nullable = false, precision = 10, scale = 2)
    private BigDecimal kmsRangeTo;

    @Column(name = "unit_price_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPricePerKm;

    @Column(name = "charge_type", nullable = false, length = 30)
    private String chargeType;

    @Column(name = "delivery_type", nullable = false, length = 30)
    private String deliveryType;

    @Column(name = "driver_type", nullable = false, length = 30)
    private String driverType;

    @Column(name = "service_type", nullable = false, length = 30)
    private String serviceType;

    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType;

    @Column(name = "fuel_type", nullable = false, length = 30)
    private String fuelType;

    @Column(name = "zone_id", nullable = false)
    private Integer zoneId;

    @Column(name = "currency_code", nullable = false, length = 30)
    private String currencyCode;

    @Column(name = "waiting_free_minutes", nullable = false)
    private Integer waitingFreeMinutes;

    @Column(name = "waiting_per_minute", nullable = false, precision = 10, scale = 2)
    private BigDecimal waitingPerMinute;

    @Column(name = "night_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightCharge;

    @Column(name = "peak_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal peakCharge;

    @Column(name = "weather_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal weatherSurcharge;

    @Column(name = "remote_area_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal remoteAreaCharge;

    @Column(name = "remote_zone_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal remoteZoneSurcharge;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}