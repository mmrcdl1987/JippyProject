package com.jippy.division.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "outlet_weekly_settlement", schema = "jippy_division")
@Getter
@Setter
public class DivOutletWeeklySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_settlement_id")
    private Integer weeklySettlementId;

    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "week_start_date")
    private LocalDate weekStartDate;

    @Column(name = "week_end_date")
    private LocalDate weekEndDate;

    @Column(name = "total_settlement_amount")
    private BigDecimal totalSettlementAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "outlet_mobile_number")
    private String outletMobileNumber;

    @Column(name = "outlet_email")
    private String outletEmail;

    @Column(name = "orders_count")
    private Integer ordersCount;

    @Column(name = "deductions")
    private BigDecimal deductions;
    @Column(name = "gst")
    private BigDecimal gst;

    @Column(name = "promotion_amount")
    private BigDecimal promotionAmount;

    @Column(name = "subscription_amount")
    private BigDecimal subscriptionAmount;

    @Column(name = "net_settlement_amount")
    private BigDecimal netSettlementAmount;

    @Column(name = "email_status")
    private String emailStatus;
}