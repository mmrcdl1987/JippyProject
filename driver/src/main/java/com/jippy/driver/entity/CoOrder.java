//package com.jippy.driver.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "orders", schema = "jippy_driver")
//@Getter
//@Setter
//public class CoOrder {
//
//    @Id
//    @Column(name = "order_id")
//    private String orderId;
//
//    @Column(name = "customer_id", nullable = false)
//    private Integer customerId;
//
//    @Column(name = "outlet_id", nullable = false)
//    private Integer outletId;
//
//    @Column(name = "driver_id")
//    private Integer driverId;
//
//    @Column(name = "order_status", nullable = false)
//    private String orderStatus;
//
//    @Column(name = "customer_delivery_address_id", nullable = false)
//    private Integer customerDeliveryAddressId;
//
//    @Column(name = "customer_phone_number", nullable = false)
//    private String customerPhoneNumber;
//
//    @Column(name = "preparation_time")
//    private LocalDateTime preparationTime;
//
//    @Column(name = "estimated_delivery_time")
//    private LocalDateTime estimatedDeliveryTime;
//
//    @Column(name = "distance_kms")
//    private Double distanceKms;
//
////  added column for COD in payment_node table
//    @Column(name = "payment_mode_id")
//    private Integer paymentModeId;
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