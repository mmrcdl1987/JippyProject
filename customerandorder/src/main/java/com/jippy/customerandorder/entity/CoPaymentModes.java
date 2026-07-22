package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "payment_modes", schema = "jippy_customer_and_order")
public class CoPaymentModes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentModeId;
    private String paymentMode;
    private Integer createdBy;
    private Integer updatedBy;
    private String createdAt;
    private String updatedAt;
}
