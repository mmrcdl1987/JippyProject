package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "refund_transactions", schema = "jippy_division")
@AllArgsConstructor
@NoArgsConstructor
public class OrderRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID refundTransactionsId;
    private String applicationOrderId;

    //private UUID transactionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    private String gatewayRefundId; //holds either Razorpay refund_id or Paytm refundId
    private Integer amountInPaise;
    private String status; // refund_status
    private String reason;
    private String bankArn; // Can hold either Razorpay bank_arn or Paytm bankTxnId
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
