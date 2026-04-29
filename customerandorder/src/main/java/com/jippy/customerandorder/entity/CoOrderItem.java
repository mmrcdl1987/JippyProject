package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", schema = "jippy_customer_and_order")
@Getter
@Setter
public class CoOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "merchant_unit_price", nullable = false)
    private BigDecimal merchantUnitPrice;

    @Column(name = "online_unit_price", nullable = false)
    private BigDecimal onlineUnitPrice;

    @Column(name = "online_price_total", nullable = false)
    private BigDecimal onlinePriceTotal;

    @Column(name = "merchant_price_total", nullable = false)
    private BigDecimal merchantPriceTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}