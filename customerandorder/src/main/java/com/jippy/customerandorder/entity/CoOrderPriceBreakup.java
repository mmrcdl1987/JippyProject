package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_price_breakup",
        schema = "jippy_customer_and_order"
)
@Getter
@Setter
public class CoOrderPriceBreakup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_price_breakup_id")
    private Integer orderPriceBreakupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CoOrder order;

    @Column(name = "coupon_id")
    private Integer couponId;

    @Column(name = "order_amount", nullable = false)
    private BigDecimal orderAmount;

    // ================= DELIVERY =================

    @Column(name = "pick_up_distance_in_kms")
    private BigDecimal pickUpDistanceKms;

    @Column(name = "delivery_distance_in_kms")
    private BigDecimal deliveryDistanceKms;

    @Column(name = "pick_up_charges")
    private BigDecimal pickUpCharges;

    /**
     * Driver delivery fee.
     */
    @Column(name = "driver_delivery_fee")
    private BigDecimal driverDeliveryFee;


    /**
     * Total delivery fee.
     */
    @Column(name = "total_delivery_fee")
    private BigDecimal totalDeliveryFee;

    /**
     * Customer delivery fee after free-distance benefit.
     */
    @Column(name = "customer_delivery_fee")
    private BigDecimal customerDeliveryFee;

    /**
     * Customer delivery GST.
     */
    @Column(name = "customer_delivery_fee_tax")
    private BigDecimal customerDeliveryFeeTax;

    // ================= PLATFORM FEE =================

    @Column(name = "platform_fee")
    private BigDecimal platformFee;

    @Column(name = "platform_fee_tax")
    private BigDecimal platformFeeTax;

    // ================= SURGE FEE =================

    @Column(name = "surge_fee")
    private BigDecimal surgeFee;

    @Column(name = "surge_fee_tax")
    private BigDecimal surgeFeeTax;

    // ================= PACKAGING FEE =================

    @Column(name = "packaging_fee")
    private BigDecimal packagingFee;

    @Column(name = "packaging_fee_tax")
    private BigDecimal packagingFeeTax;

    // ================= FOOD TAX =================

    @Column(name = "food_tax")
    private BigDecimal foodTax;

    @Column(name = "total_tax")
    private BigDecimal totalTax;

    // ================= ORDER AMOUNTS =================

    @Column(name = "order_amount_discounted")
    private BigDecimal orderAmountDiscounted;

    @Column(name = "order_total_amount", nullable = false)
    private BigDecimal orderTotalAmount;

    // ================= PAYMENT / DISCOUNT =================

    @Column(name = "wallet_amount")
    private BigDecimal walletAmount;

    @Column(name = "coupon_discount")
    private BigDecimal couponDiscount;

    @Column(name = "tip")
    private BigDecimal tip;

    // ================= AUDIT =================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}