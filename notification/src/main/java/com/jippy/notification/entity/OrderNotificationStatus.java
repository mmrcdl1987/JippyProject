package com.jippy.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_notification_status",
        schema = "jippy_notification"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_notification_status_id")
    private Integer orderNotificationStatusId;

    @Column(name = "order_id",
            nullable = false,
            length = 30)
    private String orderId;

    @Column(name = "notification_id",
            nullable = false)
    private Integer notificationId;

    @Column(name = "notification_recipient_id",
            nullable = false)
    private Integer notificationRecipientId;

    @Column(name = "recipient_type",
            nullable = false,
            length = 30)
    private String recipientType;

    @Column(name = "notification_status")
    private Boolean notificationStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}