package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "external_driver_orders", schema = "jippy_driver")
public class ExternalDriverOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "external_driver_orders_id")
    private Integer externalDriverOrdersId;

    @Column(name = "driver_order_id", nullable = false, insertable = false, updatable = false)
    private Integer driverOrderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_order_id", referencedColumnName = "driver_order_id", nullable = false)
    private DriverOrder driverOrder;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "external_delivery_id", nullable = false, length = 255)
    private String externalDeliveryId;

    @Column(name = "external_quote_id", length = 255)
    private String externalQuoteId;

    @Column(name = "tracking_url", columnDefinition = "TEXT")
    private String trackingUrl;

    @Column(name = "status", length = 50)
    private String status;

    // Hibernate 6 JSONB mapping
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_metadata", columnDefinition = "jsonb")
    private String providerMetadata; // You can also map this to a POJO class instead of String

    @Column(name = "courier_id", length = 150) // saves external driver id
    private String courierId;

    @Column(name = "courier_name", length = 150)
    private String courierName;

    @Column(name = "courier_phone", length = 30)
    private String courierPhone;

    @Column(name = "courier_vehicle_type", length = 50)
    private String courierVehicleType;

    @Column(name = "courier_license_plate", length = 50)
    private String courierLicensePlate;

    @Column(name = "delivery_charges", precision = 10, scale = 2)
    private BigDecimal deliveryCharges;

    // Stored once at pickup assignment time
    @Column(name = "courier_current_lat", precision = 10, scale = 8)
    private BigDecimal courierCurrentLat;

    @Column(name = "courier_current_lng", precision = 10, scale = 8)
    private BigDecimal courierCurrentLng;

    @Column(name = "courier_last_updated_at")
    private LocalDateTime courierLastUpdatedAt;

    @Column(name = "bearing", precision = 10, scale = 2)
    private BigDecimal bearing;

    @Column(name = "uber_delivery_charges", precision = 10, scale = 2)
    private BigDecimal uber_delivery_charges;

    @Column(name = "uber_base_fare", precision = 10, scale = 2)
    private BigDecimal uber_base_fare;

    @Column(name = "uber_rain_surge", precision = 10, scale = 2)
    private BigDecimal uber_rain_surge;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
