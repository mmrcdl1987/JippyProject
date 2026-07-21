package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payment_transactions", schema = "jippy_division")
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_transactions_id", updatable = false, nullable = false)
    private UUID paymentTransactionsId;

    @Column(name = "application_order_id", nullable = false, length = 30)
    private String applicationOrderId;

    @Column(name = "payment_method_type", nullable = false, length = 30)
    private Integer paymentMethodType; // E.g., "RAZORPAY", "PAYTM"

    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId; // Razorpay Order ID (Null for Paytm)

    @Column(name = "gateway_payment_id", length = 255)
    private String gatewayPaymentId; // Razorpay Payment ID or Paytm TXNID

    @Column(name = "gateway_signature", length = 512)
    private String gatewaySignature; // Razorpay Signature or Paytm CHECKSUMHASH

    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "PENDING";

    @Column(name = "amount", nullable = false)
    private Integer amount; // Stored in Paise (Rs. 10.50 -> 1050)

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "txn_rrn", length = 100)
    private String txnRrn; // Razorpay rrn / Paytm BANKTXNID

    @Column(name = "bank_auth_code", length = 50)
    private String bankAuthCode; // Razorpay bank_auth_code / Paytm RESPCODE

    @Column(name = "bank_name", length = 100)
    private String bankName; // E.g., HDFC, SBI, WALLET

    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse; // Complete raw JSON block for audit trail

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
