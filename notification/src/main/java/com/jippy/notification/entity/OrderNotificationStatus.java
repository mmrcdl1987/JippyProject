package com.jippy.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_notification_status", schema = "jippy_customer_and_order")
@Getter
@Setter
public class OrderNotificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_notification_status_id")
    private Integer orderNotificationStatusId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "notification_status")
    private Boolean notificationStatus;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}