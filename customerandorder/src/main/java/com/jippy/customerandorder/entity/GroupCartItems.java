package com.jippy.customerandorder.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_cart_items", schema = "jippy_customer_and_order")
@Data
public class GroupCartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_cart_item_id")
    private Integer groupCartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_orders_invitation_id", nullable = false)
    private GroupOrderInvitation groupOrders;

    // Foreign key to ensure it's a valid customer adding items
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false)
    private CoCustomer customer;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "merchant_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal merchantUnitPrice;

    @Column(name = "online_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal onlineUnitPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Integer createdBy;

    private Integer updatedBy;
}
