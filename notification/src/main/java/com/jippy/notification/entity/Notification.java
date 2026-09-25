
        package com.jippy.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        schema = "jippy_notification"
)
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "message", nullable = false, length = 100)
    private String message;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
