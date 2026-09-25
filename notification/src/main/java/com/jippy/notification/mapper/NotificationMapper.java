package com.jippy.notification.mapper;

import com.jippy.notification.dto.AdminNotificationCreateRequestDto;
import com.jippy.notification.dto.AdminNotificationUpdateRequestDto;
import com.jippy.notification.dto.AdminNotificationResponseDto;
import com.jippy.notification.entity.Notification;

import java.time.LocalDateTime;

public final class NotificationMapper {

    private NotificationMapper() {
        // Utility class
    }

    public static Notification toEntity(AdminNotificationCreateRequestDto request) {
        if (request == null) {
            return null;
        }

        Notification notification = new Notification();

        notification.setNotificationType(normalizeUpper(request.getNotificationType()));

        notification.setRole(normalizeUpper(request.getRole()));

        notification.setSubject(normalize(request.getSubject()));

        notification.setMessage(normalize(request.getMessage()));

        notification.setImageUrl(normalizeNullable(request.getImageUrl()));

        notification.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

        notification.setPriority(normalizeUpper(request.getPriority()));

        notification.setCreatedAt(LocalDateTime.now());

        return notification;
    }

    public static AdminNotificationResponseDto toDto(Notification entity) {
        if (entity == null) {
            return null;
        }

        AdminNotificationResponseDto dto = new AdminNotificationResponseDto();

        dto.setNotificationId(entity.getNotificationId());

        dto.setNotificationType(entity.getNotificationType());

        dto.setRole(entity.getRole());

        dto.setSubject(entity.getSubject());

        dto.setMessage(entity.getMessage());

        dto.setImageUrl(entity.getImageUrl());

        dto.setIsActive(entity.getIsActive());

        dto.setPriority(entity.getPriority());

        dto.setCreatedAt(entity.getCreatedAt());

        dto.setCreatedBy(entity.getCreatedBy());

        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public static void updateEntity(AdminNotificationUpdateRequestDto request, Notification entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setNotificationType(normalizeUpper(request.getNotificationType()));

        entity.setRole(normalizeUpper(request.getRole()));

        entity.setSubject(normalize(request.getSubject()));

        entity.setMessage(normalize(request.getMessage()));

        entity.setImageUrl(normalizeNullable(request.getImageUrl()));

        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        entity.setPriority(normalizeUpper(request.getPriority()));

        entity.setUpdatedAt(LocalDateTime.now());
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}