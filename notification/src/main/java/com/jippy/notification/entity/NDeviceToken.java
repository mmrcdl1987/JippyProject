package com.jippy.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "device_tokens",
        schema = "jippy_notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_tokens_fcm_token",
                        columnNames = {"fcm_token"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_device_tokens_user_lookup",
                        columnList = "user_id, user_type"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Integer deviceTokenId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 500)
    private String fcmToken;

    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}