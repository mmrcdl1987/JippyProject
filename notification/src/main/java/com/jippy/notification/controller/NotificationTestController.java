package com.jippy.notification.controller;

import com.jippy.notification.dto.TestNotificationRequest;
import com.jippy.notification.dto.TestNotificationResponse;
import com.jippy.notification.service.FirebaseNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification/test")
@RequiredArgsConstructor
@Slf4j
public class NotificationTestController {

    private final FirebaseNotificationService firebaseNotificationService;

    // TEMPORARY ENDPOINT FOR TESTING
    @PostMapping("/send")
    public ResponseEntity<TestNotificationResponse> sendTestNotification(@Valid @RequestBody TestNotificationRequest request) {
        log.info("TEST_NOTIFICATION_REQUEST_RECEIVED | title={}", request.getTitle());

        try {
            String messageId = firebaseNotificationService.sendNotification(request.getToken(), request.getTitle(), request.getBody());
            log.info("TEST_NOTIFICATION_SENT");
            return ResponseEntity.ok(new TestNotificationResponse(true, "Test notification sent successfully", messageId));
        } catch (Exception e) {
            log.error("TEST_NOTIFICATION_FAILED | error={}", e.getMessage());
            return ResponseEntity.internalServerError().body(new TestNotificationResponse(false, "Test notification failed: " + e.getMessage(), null));
        }
    }
}
