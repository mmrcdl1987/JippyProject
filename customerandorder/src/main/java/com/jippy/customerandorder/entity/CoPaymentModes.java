package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_modes",
        schema = "jippy_customer_and_order"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoPaymentModes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_mode_id")
    private Integer paymentModeId;

    @Column(name = "payment_mode", nullable = false, length = 50)
    private String paymentMode;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}