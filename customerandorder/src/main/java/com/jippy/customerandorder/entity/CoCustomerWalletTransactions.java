package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "customer_wallet_transactions",
        schema = "jippy_customer_and_order"
)
public class CoCustomerWalletTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_wallet_transactions_id")
    private Integer customerWalletTransactionsId;

    @Column(name = "wallet_id")
    private Integer walletId;

    /**
     * Order that generated this wallet points transaction.
     * Used to prevent the same order from receiving
     * ORDER_VALUE_POINTS more than once.
     */
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "points")
    private Integer points;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}