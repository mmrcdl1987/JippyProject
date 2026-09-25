package com.jippy.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin Notification Create Request")
public class AdminNotificationCreateRequestDto {

    @NotBlank(message = "Notification type is required")
    @Size(max = 50, message = "Notification type must not exceed 50 characters")
    @Schema(description = "Type of notification", example = "ORDER_PLACED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String notificationType;

    @NotBlank(message = "Role is required")
    @Size(max = 100, message = "Role must not exceed 100 characters")
    @Schema(description = "Target user role", example = "CUSTOMER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    @Schema(description = "Notification title/subject", example = "Order Placed", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 100, message = "Message must not exceed 100 characters")
    @Schema(description = "Notification message body", example = "Your order has been placed successfully.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    @Schema(description = "Optional image URL", example = "https://example.com/banner.png", nullable = true)
    private String imageUrl;

    @Schema(description = "Active status", example = "true", defaultValue = "true")
    private Boolean isActive = true;

    @NotBlank(message = "Priority is required")
    @Size(max = 20, message = "Priority must not exceed 20 characters")
    @Schema(description = "Priority level (LOW, MEDIUM, HIGH, CRITICAL)", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String priority;
}
