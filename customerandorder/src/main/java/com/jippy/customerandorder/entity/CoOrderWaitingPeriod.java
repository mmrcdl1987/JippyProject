package com.jippy.customerandorder.entity;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_waiting_period",
        schema = "jippy_customer_and_order")
@Data
public class CoOrderWaitingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_waiting_period_id")
    private Integer orderWaitingPeriodId;

    @Column(name = "order_rejection_id")
    private Integer orderRejectionId;

    @Column(name = "allows_rejection")
    private Boolean allowsRejection;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}