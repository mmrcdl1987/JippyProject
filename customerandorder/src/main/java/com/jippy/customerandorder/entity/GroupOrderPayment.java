package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "group_order_payments", schema = "jippy_customer_and_order")
public class GroupOrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_order_payment_id")
    private Integer groupOrderPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_orders_invitation_id", nullable = false)
    private GroupOrderInvitation groupOrderInvitation;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "amount_to_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountToPay;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus; // PENDING, PAID, FAILED

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;
}
