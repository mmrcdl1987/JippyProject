/*
package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

*/
/**
 * Entity for driver_wallet_transactions table
 *//*

@Entity
@Table(name = "driver_wallet_transactions", schema = "jippy_customer_and_order")
@Getter
@Setter
public class CoDriverWalletTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_wallet_transactions_id")
    private Integer driverWalletTransactionsId;

    @Column(name = "driver_wallet_id", nullable = false)
    private Integer driverWalletId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "cod_amount", nullable = false)
    private BigDecimal codAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}*/
