package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_settings", schema = "jippy_customer_and_order")
@Data
public class OrderSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_settings_id")
    private Integer orderSettingsId;

    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee;

    @Column(name = "surge_fee", nullable = false)
    private BigDecimal surgeFee;

    @Column(name = "packaging_fee", nullable = false)
    private BigDecimal packagingFee;

    @Column(name = "delivery_fee_tax", nullable = false)
    private BigDecimal deliveryFeeTax;

    @Column(name = "food_total_amount_tax", nullable = false)
    private BigDecimal foodTotalAmountTax;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}