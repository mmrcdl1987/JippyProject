package com.jippy.driver.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.locationtech.jts.geom.LineString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_orders", schema = "jippy_driver")
@Data
public class DriverOrder {

    // Primary key of driver_orders table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_order_id")
    private Integer driverOrderId;

    @Column(name = "driver_id", nullable = false)
    private Integer driverId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "driver_id", nullable = false)
//    private Driver driver;

    // Order id mapped from orders table
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "driver_type", nullable = false, length = 30)
    private String driverType = "JIPPY_DRIVER";

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    // Pick up distance in kilometers
    @Column(name = "pick_up_distance_in_kms", precision = 10, scale = 2)
    private BigDecimal pickUpDistanceInKms;

    // Delivery distance in kilometers
    @Column(name = "delivery_distance_in_kms", precision = 10, scale = 2)
    private BigDecimal deliveryDistanceInKms;

    // Pick up charges
    @Column(name = "pick_up_charges", precision = 10, scale = 2)
    private BigDecimal pickUpCharges;

    // Delivery charges
    @Column(name = "deliver_charges", precision = 10, scale = 2)
    private BigDecimal deliverCharges;

    // Total delivery fee
    @Column(name = "total_delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDeliveryFee;

    // Surge fee
    @Column(name = "surge_fee", precision = 10, scale = 2)
    private BigDecimal surgeFee;

    // Tips given by customer
    @Column(name = "tips", precision = 10, scale = 2)
    private BigDecimal tips;

    // Record created timestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Created by user id
    @Column(name = "created_by")
    private Integer createdBy;

    // Record updated timestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Updated by user id
    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(columnDefinition = "geometry(LineStringM, 4326)")
    private LineString deliveryRoute;

    // One-to-One bidirectional mapping with ExternalDriverOrder
    @ToString.Exclude // <--- Add this annotation on the relational field
    @OneToOne(mappedBy = "driverOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ExternalDriverOrder externalDriverOrder;


}