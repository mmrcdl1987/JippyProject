package com.jippy.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_notification_status",
        schema = "jippy_notification"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderNotificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_notification_status_id")
    private Integer orderNotificationStatusId;

    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "notification_id", nullable = false)
    private Integer notificationId;

    @Column(name = "notification_recipient_id", nullable = false)
    private Integer notificationRecipientId;

    @Column(name = "recipient_type", nullable = false, length = 30)
    private String recipientType;

    @Column(name = "notification_status")
    private Boolean notificationStatus;

    @Column(name = "device_token_id")
    private Integer deviceTokenId;

    @Column(name = "firebase_message_id")
    private String firebaseMessageId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}