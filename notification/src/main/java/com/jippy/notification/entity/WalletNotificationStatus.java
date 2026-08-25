package com.jippy.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet_notification_status",
        schema = "jippy_notification"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletNotificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_notification_status_id")
    private Integer walletNotificationStatusId;

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
