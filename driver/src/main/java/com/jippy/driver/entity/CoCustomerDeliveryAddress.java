//package com.jippy.driver.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import org.locationtech.jts.geom.Point;
//
//import java.time.LocalDateTime;
//
//
//@Entity
//@Table(name = "customer_delivery_addresses", schema = "jippy_driver")
//@Getter
//@Setter
//public class CoCustomerDeliveryAddress {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "customer_address_id")
//    private Integer customerAddressId;
//
//    @Column(name = "customer_id", nullable = false)
//    private Integer customerId;
//
//    @Column(
//            name = "location",
//            columnDefinition = "GEOGRAPHY(POINT)"
//    )
//    private Point location;
//
//    private String doorNo;
//    private String buildingName;
//    private String laneNo;
//
//    private Integer area;
//    private Integer city;
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