package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_checkout_tax", schema = "jippy_customer_and_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoOrderCheckoutTax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_checkout_tax_id")
    private Integer orderCheckoutTaxId;

    @Column(name = "platform_fee_tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal platformFeeTax;

    @Column(name = "surge_fee_tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal surgeFeeTax;

    @Column(name = "packaging_fee_tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal packagingFeeTax;

    @Column(name = "delivery_fee_tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal deliveryFeeTax;

    @Column(name = "food_amount_tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal foodAmountTax;

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