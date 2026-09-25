
        package com.jippy.notification.service;

import com.jippy.notification.dto.AdminNotificationCreateRequestDto;
import com.jippy.notification.dto.AdminNotificationResponseDto;
import com.jippy.notification.dto.AdminNotificationUpdateRequestDto;
import com.jippy.notification.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface NotificationAdminService {

    AdminNotificationResponseDto createNotification(
            AdminNotificationCreateRequestDto request
    );

    AdminNotificationResponseDto getNotificationById(
            Integer notificationId
    );

    PageResponseDto<AdminNotificationResponseDto> getNotifications(
            String notificationType,
            String role,
            String priority,
            Boolean isActive,
            Pageable pageable
    );

    AdminNotificationResponseDto updateNotification(
            Integer notificationId,
            AdminNotificationUpdateRequestDto request
    );

    AdminNotificationResponseDto updateNotificationStatus(
            Integer notificationId,
            Boolean active
    );
}
