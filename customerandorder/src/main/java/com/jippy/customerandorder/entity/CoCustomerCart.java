package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_cart", schema = "jippy_customer_and_order")
@Data
public class CoCustomerCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_option_id")
    private Integer variantOptionId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}