package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cancelled_orders_inventory",
        schema = "jippy_fm")
@Getter
@Setter
public class FmCancelledOrdersInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancelled_orders_inventory_id")
    private Integer cancelledOrdersInventoryId;

    @Column(name = "specialized_outlet_id")
    private Integer specializedOutletId;

    @Column(name = "cancelled_order_id")
    private String cancelledOrderId;

    @Column(name = "driver_id")
    private Integer driverId;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}