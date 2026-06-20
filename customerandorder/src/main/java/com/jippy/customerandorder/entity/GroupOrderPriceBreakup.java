package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_order_price_breakup", schema = "jippy_customer_and_order")
public class GroupOrderPriceBreakup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_order_price_breakup_id")
    private Integer groupOrderPriceBreakupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_orders_invitation_id", nullable = false)
    private GroupOrderInvitation groupOrderInvitation;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "deliver_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliverCharges;

    @Column(name = "surge_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal surgeFee;

    @Column(name = "packaging_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagingFee;

    @Column(name = "gst", nullable = false, precision = 10, scale = 2)
    private BigDecimal gst;

    @Column(name = "order_total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderTotalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
