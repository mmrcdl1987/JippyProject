//package com.jippy.driver.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "order_price_breakup", schema = "jippy_driver")
//@Getter
//@Setter
//public class CoOrderPriceBreakup {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "order_price_breakup_id")
//    private Long orderPriceBreakupId;
//
//    @Column(name = "order_id", nullable = false)
//    private String orderId;
//
//    @Column(name = "coupon_id")
//    private Integer couponId;
//
//    @Column(name = "order_amount", nullable = false)
//    private BigDecimal orderAmount;
//
//    @Column(name = "platform_fee")
//    private BigDecimal platformFee;
//
//    @Column(name = "delivery_fee")
//    private BigDecimal deliveryFee;
//
//    @Column(name = "surge_fee")
//    private BigDecimal surgeFee;
//
//    @Column(name = "packaging_fee")
//    private BigDecimal packagingFee;
//
//    @Column(name = "gst")
//    private BigDecimal gst;
//
//    @Column(name = "order_total_amount", nullable = false)
//    private BigDecimal orderTotalAmount;
//
//    @Column(name = "coupon_discount")
//    private BigDecimal couponDiscount;
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