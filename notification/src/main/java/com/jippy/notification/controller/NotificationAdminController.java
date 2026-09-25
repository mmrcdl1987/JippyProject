package com.jippy.notification.controller;

import com.jippy.notification.dto.*;
import com.jippy.notification.service.NotificationAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification/admin")
@RequiredArgsConstructor
@Slf4j
public class NotificationAdminController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private static final String[] ALLOWED_SORT_FIELDS = {"notificationId", "notificationType", "role", "subject", "priority", "isActive", "createdAt", "updatedAt"};

    private final NotificationAdminService notificationAdminService;

    // CREATE
    @PostMapping
    public ResponseEntity<AdminApiResponse<AdminNotificationResponseDto>> createNotification(@Valid @RequestBody AdminNotificationCreateRequestDto request) {

        log.info("ADMIN API | CREATE_NOTIFICATION | type={} | role={}", request.getNotificationType(), request.getRole());

        AdminNotificationResponseDto response = notificationAdminService.createNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AdminApiResponse.success("Notification created successfully", response));
    }

    // GET BY ID
    @GetMapping("/{notificationId}")
    public ResponseEntity<AdminApiResponse<AdminNotificationResponseDto>> getNotificationById(@PathVariable Integer notificationId) {

        log.info("ADMIN API | GET_NOTIFICATION | notificationId={}", notificationId);

        AdminNotificationResponseDto response = notificationAdminService.getNotificationById(notificationId);

        return ResponseEntity.ok(AdminApiResponse.success("Notification retrieved successfully", response));
    }

    // GET ALL / FILTER / PAGINATION

    @GetMapping
    public ResponseEntity<AdminApiResponse<PageResponseDto<AdminNotificationResponseDto>>> getNotifications(

            @RequestParam(required = false) String notificationType,

            @RequestParam(required = false) String role,

            @RequestParam(required = false) String priority,

            @RequestParam(required = false) Boolean isActive,

            @RequestParam(defaultValue = "0") Integer page,

            @RequestParam(defaultValue = "20") Integer size,

            @RequestParam(defaultValue = "createdAt") String sortBy,

            @RequestParam(defaultValue = "DESC") String sortDirection) {

        validatePagination(page, size);

        Sort sort = parseSort(sortBy, sortDirection);

        Pageable pageable = PageRequest.of(page, size, sort);

        log.info("ADMIN API | GET_NOTIFICATIONS | type={} | role={} | priority={} | isActive={} | page={} | size={} | sortBy={} | direction={}", notificationType, role, priority, isActive, page, size, sortBy, sortDirection);

        PageResponseDto<AdminNotificationResponseDto> response = notificationAdminService.getNotifications(notificationType, role, priority, isActive, pageable);

        return ResponseEntity.ok(AdminApiResponse.success("Notifications retrieved successfully", response));
    }
    // UPDATE
    @PutMapping("/{notificationId}")
    public ResponseEntity<AdminApiResponse<AdminNotificationResponseDto>> updateNotification(

            @PathVariable Integer notificationId,

            @Valid @RequestBody AdminNotificationUpdateRequestDto request) {

        log.info("ADMIN API | UPDATE_NOTIFICATION | notificationId={}", notificationId);

        AdminNotificationResponseDto response = notificationAdminService.updateNotification(notificationId, request);

        return ResponseEntity.ok(AdminApiResponse.success("Notification updated successfully", response));
    }
    // ACTIVATE / DEACTIVATE
    @PatchMapping("/{notificationId}/status")
    public ResponseEntity<AdminApiResponse<AdminNotificationResponseDto>> updateNotificationStatus(
            @PathVariable Integer notificationId,
            @Valid @RequestBody AdminNotificationStatusUpdateRequestDto request) {

        log.info(
                "ADMIN API | UPDATE_NOTIFICATION_STATUS | notificationId={} | active={}",
                notificationId,
                request.getActive()
        );

        AdminNotificationResponseDto response =
                notificationAdminService.updateNotificationStatus(
                        notificationId,
                        request.getActive()
                );

        String message = Boolean.TRUE.equals(request.getActive())
                ? "Notification activated successfully"
                : "Notification deactivated successfully";

        return ResponseEntity.ok(
                AdminApiResponse.success(message, response)
        );
    }


    // PAGINATION VALIDATION

    private void validatePagination(Integer page, Integer size) {

        if (page == null || page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (size == null || size <= 0 || size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
    // SORT VALIDATION
    private Sort parseSort(String sortBy, String sortDirection) {

        String validSortField = isAllowedSortField(sortBy) ? sortBy : DEFAULT_SORT_FIELD;

        Sort.Direction direction;

        try {

            direction = Sort.Direction.fromString(sortDirection);

        } catch (IllegalArgumentException ex) {

            direction = DEFAULT_SORT_DIRECTION;
        }

        return Sort.by(direction, validSortField);
    }

    private boolean isAllowedSortField(String sortBy) {

        if (sortBy == null || sortBy.isBlank()) {
            return false;
        }

        for (String allowedField : ALLOWED_SORT_FIELDS) {

            if (allowedField.equals(sortBy)) {
                return true;
            }
        }

        return false;
    }
}
