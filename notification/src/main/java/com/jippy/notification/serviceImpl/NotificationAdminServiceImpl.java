package com.jippy.notification.serviceImpl;

import com.jippy.notification.dto.AdminNotificationCreateRequestDto;
import com.jippy.notification.dto.AdminNotificationResponseDto;
import com.jippy.notification.dto.AdminNotificationUpdateRequestDto;
import com.jippy.notification.dto.PageResponseDto;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.enums.NotificationPriority;
import com.jippy.notification.enums.NotificationRole;
import com.jippy.notification.enums.NotificationType;
import com.jippy.notification.exception.DuplicateNotificationException;
import com.jippy.notification.exception.InvalidNotificationPriorityException;
import com.jippy.notification.exception.InvalidNotificationRoleException;
import com.jippy.notification.exception.InvalidNotificationTypeException;
import com.jippy.notification.exception.NotificationNotFoundException;
import com.jippy.notification.mapper.NotificationMapper;
import com.jippy.notification.repository.NotificationRepository;
import com.jippy.notification.service.NotificationAdminService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAdminServiceImpl implements NotificationAdminService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public AdminNotificationResponseDto createNotification(AdminNotificationCreateRequestDto request) {

        log.info("ADMIN | CREATE_NOTIFICATION_START | type={} | role={}", request.getNotificationType(), request.getRole());

        validateEnums(request.getNotificationType(), request.getRole(), request.getPriority());

        String notificationType = request.getNotificationType().trim().toUpperCase();

        String role = request.getRole().trim().toUpperCase();

        if (notificationRepository.existsByNotificationTypeAndRoleAndIsActiveTrue(notificationType, role)) {

            log.warn("ADMIN | DUPLICATE_NOTIFICATION_DETECTED | type={} | role={}", notificationType, role);

            throw new DuplicateNotificationException("Notification template already exists for type '" + notificationType + "' and role '" + role + "'");
        }

        Notification notification = NotificationMapper.toEntity(request);

        Notification savedNotification = notificationRepository.save(notification);

        log.info("ADMIN | CREATE_NOTIFICATION_SUCCESS | notificationId={}", savedNotification.getNotificationId());

        return NotificationMapper.toDto(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminNotificationResponseDto getNotificationById(Integer notificationId) {

        log.info("ADMIN | GET_NOTIFICATION_BY_ID | notificationId={}", notificationId);

        Notification notification = findNotification(notificationId);

        return NotificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminNotificationResponseDto> getNotifications(String notificationType, String role, String priority, Boolean isActive, Pageable pageable) {

        log.info("ADMIN | GET_NOTIFICATIONS_FILTERED | type={} | role={} | priority={} | isActive={} | page={} | size={}", notificationType, role, priority, isActive, pageable.getPageNumber(), pageable.getPageSize());

        Specification<Notification> specification = buildNotificationSpecification(notificationType, role, priority, isActive);

        Page<Notification> notificationPage = notificationRepository.findAll(specification, pageable);

        List<AdminNotificationResponseDto> content = notificationPage.map(NotificationMapper::toDto).getContent();

        return new PageResponseDto<>(content, notificationPage.getNumber(), notificationPage.getSize(), notificationPage.getTotalElements(), notificationPage.getTotalPages(), notificationPage.isFirst(), notificationPage.isLast(), notificationPage.isEmpty());
    }

    @Override
    @Transactional
    public AdminNotificationResponseDto updateNotification(Integer notificationId, AdminNotificationUpdateRequestDto request) {

        log.info("ADMIN | UPDATE_NOTIFICATION_START | notificationId={}", notificationId);

        Notification notification = findNotification(notificationId);

        validateEnums(request.getNotificationType(), request.getRole(), request.getPriority());

        String notificationType = request.getNotificationType().trim().toUpperCase();

        String role = request.getRole().trim().toUpperCase();

        if (notificationRepository.existsByNotificationTypeAndRoleAndIsActiveTrueAndNotificationIdNot(notificationType, role, notificationId)) {

            log.warn("ADMIN | DUPLICATE_NOTIFICATION_DETECTED | type={} | role={} | notificationId={}", notificationType, role, notificationId);

            throw new DuplicateNotificationException("Notification template already exists for type '" + notificationType + "' and role '" + role + "'");
        }

        NotificationMapper.updateEntity(request, notification);

        Notification updatedNotification = notificationRepository.save(notification);

        log.info("ADMIN | UPDATE_NOTIFICATION_SUCCESS | notificationId={}", updatedNotification.getNotificationId());

        return NotificationMapper.toDto(updatedNotification);
    }

    @Override
    @Transactional
    public AdminNotificationResponseDto updateNotificationStatus(Integer notificationId, Boolean active) {

        log.info("ADMIN | UPDATE_NOTIFICATION_STATUS_START | notificationId={} | active={}", notificationId, active);

        Notification notification = findNotification(notificationId);

        /*
         * No change required.
         * Return the existing notification as-is.
         */
        if (Boolean.TRUE.equals(notification.getIsActive()) && Boolean.TRUE.equals(active)) {

            log.info("ADMIN | NOTIFICATION_ALREADY_ACTIVE | notificationId={}", notificationId);

            return NotificationMapper.toDto(notification);
        }

        if (Boolean.FALSE.equals(notification.getIsActive()) && Boolean.FALSE.equals(active)) {

            log.info("ADMIN | NOTIFICATION_ALREADY_INACTIVE | notificationId={}", notificationId);

            return NotificationMapper.toDto(notification);
        }

        /*
         * Activation
         *
         * Only one active notification template is allowed
         * for the same notificationType + role.
         */
        if (Boolean.TRUE.equals(active)) {

            if (notificationRepository.existsByNotificationTypeAndRoleAndIsActiveTrue(notification.getNotificationType(), notification.getRole())) {

                log.warn("ADMIN | ACTIVATE_DUPLICATE_NOTIFICATION | " + "type={} | role={} | notificationId={}", notification.getNotificationType(), notification.getRole(), notificationId);

                throw new DuplicateNotificationException("An active notification already exists for type '" + notification.getNotificationType() + "' and role '" + notification.getRole() + "'");
            }
        }

         //Update status
        notification.setIsActive(active);
        notification.setUpdatedAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);

        log.info("ADMIN | UPDATE_NOTIFICATION_STATUS_SUCCESS | " + "notificationId={} | active={}", notificationId, active);

        return NotificationMapper.toDto(updatedNotification);
    }


    private Notification findNotification(Integer notificationId) {

        return notificationRepository.findById(notificationId).orElseThrow(() -> {

            log.warn("ADMIN | NOTIFICATION_NOT_FOUND | notificationId={}", notificationId);

            return new NotificationNotFoundException("Notification template not found with ID: " + notificationId);
        });
    }

    private Specification<Notification> buildNotificationSpecification(String notificationType, String role, String priority, Boolean isActive) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (notificationType != null && !notificationType.isBlank()) {

                predicates.add(cb.equal(cb.upper(root.get("notificationType")), notificationType.trim().toUpperCase()));
            }

            if (role != null && !role.isBlank()) {

                predicates.add(cb.equal(cb.upper(root.get("role")), role.trim().toUpperCase()));
            }

            if (priority != null && !priority.isBlank()) {

                predicates.add(cb.equal(cb.upper(root.get("priority")), priority.trim().toUpperCase()));
            }

            if (isActive != null) {

                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateEnums(String notificationType, String role, String priority) {

        if (!NotificationType.isValid(notificationType)) {

            log.warn("ADMIN | INVALID_NOTIFICATION_TYPE | type={}", notificationType);

            throw new InvalidNotificationTypeException("Unsupported notification type: '" + notificationType + "'");
        }

        if (!NotificationRole.isValid(role)) {

            log.warn("ADMIN | INVALID_NOTIFICATION_ROLE | role={}", role);

            throw new InvalidNotificationRoleException("Unsupported role: '" + role + "'. Supported roles: CUSTOMER, DRIVER, MERCHANT, ADMIN");
        }

        if (!NotificationPriority.isValid(priority)) {

            log.warn("ADMIN | INVALID_NOTIFICATION_PRIORITY | priority={}", priority);

            throw new InvalidNotificationPriorityException("Unsupported priority: '" + priority + "'. Supported priorities: LOW, MEDIUM, HIGH, CRITICAL");
        }
    }
}
