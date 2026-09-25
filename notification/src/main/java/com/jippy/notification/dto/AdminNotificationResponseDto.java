package com.jippy.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin Notification Details Response")
public class AdminNotificationResponseDto {

    @Schema(description = "Notification ID", example = "1")
    private Integer notificationId;

    @Schema(description = "Type of notification", example = "ORDER_PLACED")
    private String notificationType;

    @Schema(description = "Target user role", example = "CUSTOMER")
    private String role;

    @Schema(description = "Notification title/subject", example = "Order Placed")
    private String subject;

    @Schema(description = "Notification message body", example = "Your order has been placed successfully.")
    private String message;

    @Schema(description = "Image URL", example = "https://example.com/banner.png", nullable = true)
    private String imageUrl;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Timestamp when created", example = "2026-09-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "User ID who created the record", example = "1")
    private Integer createdBy;

    @Schema(description = "Timestamp when last updated", example = "2026-09-23T12:00:00", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "User ID who last updated the record", example = "1", nullable = true)
    private Integer updatedBy;
}
