package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatus {

    @Id
    @Column(name = "customer_status_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerStatusId;

    @Column(name = "status_name", nullable = false)
    private String statusName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}