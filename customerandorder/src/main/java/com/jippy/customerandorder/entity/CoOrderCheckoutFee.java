package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_checkout_fee", schema = "jippy_customer_and_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoOrderCheckoutFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_checkout_fee_id")
    private Integer orderCheckoutFeeId;

    @Column(name = "platform_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal platformFee;

    @Column(name = "platform_fee_toggle", nullable = false)
    private Boolean platformFeeToggle;

    @Column(name = "surge_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal surgeFee;

    @Column(name = "surge_fee_toggle", nullable = false)
    private Boolean surgeFeeToggle;

    @Column(name = "packaging_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal packagingFee;

    @Column(name = "packaging_fee_toggle", nullable = false)
    private Boolean packagingFeeToggle;

    @Column(name = "area_id", nullable = false)
    private Integer areaId;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}