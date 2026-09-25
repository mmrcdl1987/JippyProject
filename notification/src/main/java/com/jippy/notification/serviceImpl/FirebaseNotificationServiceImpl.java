package com.jippy.notification.serviceImpl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.jippy.notification.exception.NotificationException;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.service.FirebaseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public String sendNotification(String token, String title, String body) {
        return sendNotification(token, title, body, null);
    }

    @Override
    @Transactional
    public String sendNotification(String token, String title, String body, Map<String, String> dataPayload) {
        if (token == null || token.isBlank()) {
            log.warn("FCM_SEND_SKIPPED | Token is empty");
            throw new NotificationException("Device token cannot be empty");
        }

        try {
            Notification fcmNotification = Notification.builder().setTitle(title).setBody(body).build();

            Message.Builder msgBuilder = Message.builder().setToken(token).setNotification(fcmNotification);

            if (dataPayload != null && !dataPayload.isEmpty()) {
                dataPayload.forEach((k, v) -> {
                    if (k != null && v != null) {
                        msgBuilder.putData(k, v);
                    }
                });
            }

            String response = FirebaseMessaging.getInstance().send(msgBuilder.build());
            log.info("FCM_SEND_SUCCESS | token={} | messageId={}", maskToken(token), response);
            return response;

        } catch (FirebaseMessagingException fme) {
            log.error("FCM_SEND_FAILED | errorCode={} | msg={}", fme.getMessagingErrorCode(), fme.getMessage());
            handleDeadTokenCleanup(token, fme);
            throw new NotificationException("FCM dispatch failed: " + fme.getMessage(), fme);
        } catch (Exception ex) {
            log.error("FCM_SEND_UNEXPECTED_ERROR | msg={}", ex.getMessage(), ex);
            throw new NotificationException("Unexpected error during FCM dispatch", ex);
        }
    }

    @Override
    public String sendTopicNotification(String topic, String title, String body, Map<String, String> dataPayload) {
        if (topic == null || topic.isBlank()) {
            log.warn("FCM_TOPIC_SEND_SKIPPED | Topic is empty");
            throw new NotificationException("Firebase topic cannot be empty");
        }

        try {
            Notification fcmNotification = Notification.builder().setTitle(title).setBody(body).build();

            Message.Builder msgBuilder = Message.builder().setTopic(topic).setNotification(fcmNotification);

            if (dataPayload != null && !dataPayload.isEmpty()) {
                dataPayload.forEach((k, v) -> {
                    if (k != null && v != null) {
                        msgBuilder.putData(k, v);
                    }
                });
            }

            String response = FirebaseMessaging.getInstance().send(msgBuilder.build());
            log.info("FCM_TOPIC_SEND_SUCCESS | topic={} | messageId={}", topic, response);
            return response;

        } catch (FirebaseMessagingException fme) {
            log.error("FCM_TOPIC_SEND_FAILED | topic={} | error={}", topic, fme.getMessage(), fme);
            throw new NotificationException("FCM topic dispatch failed: " + fme.getMessage(), fme);
        } catch (Exception ex) {
            log.error("FCM_TOPIC_UNEXPECTED_ERROR | topic={} | msg={}", topic, ex.getMessage(), ex);
            throw new NotificationException("Unexpected error during FCM topic dispatch", ex);
        }
    }

    @Override
    public String sendTopicDataMessage(String topic, Map<String, String> dataPayload) {
        if (topic == null || topic.isBlank()) {
            log.warn("FCM_TOPIC_DATA_SEND_SKIPPED | Topic is empty");
            throw new NotificationException("Firebase topic cannot be empty");
        }

        try {
            Message.Builder msgBuilder = Message.builder().setTopic(topic);

            if (dataPayload != null && !dataPayload.isEmpty()) {
                dataPayload.forEach((k, v) -> {
                    if (k != null && v != null) {
                        msgBuilder.putData(k, v);
                    }
                });
            }

            String response = FirebaseMessaging.getInstance().send(msgBuilder.build());
            log.info("FCM_TOPIC_DATA_SEND_SUCCESS | topic={} | messageId={}", topic, response);
            return response;

        } catch (FirebaseMessagingException fme) {
            log.error("FCM_TOPIC_DATA_SEND_FAILED | topic={} | error={}", topic, fme.getMessage(), fme);
            throw new NotificationException("FCM topic data dispatch failed: " + fme.getMessage(), fme);
        } catch (Exception ex) {
            log.error("FCM_TOPIC_DATA_UNEXPECTED_ERROR | topic={} | msg={}", topic, ex.getMessage(), ex);
            throw new NotificationException("Unexpected error during FCM topic data dispatch", ex);
        }
    }

    private void handleDeadTokenCleanup(String token, FirebaseMessagingException fme) {
        MessagingErrorCode errorCode = fme.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            try {
                log.warn("EVICTING_DEAD_TOKEN | token={} | reason={}", maskToken(token), errorCode);
                deviceTokenRepository.deleteByFcmToken(token);
                log.info("DEAD_TOKEN_EVICTED_SUCCESS | token={}", maskToken(token));
            } catch (Exception e) {
                log.error("DEAD_TOKEN_EVICTION_FAILED | token={} | error={}", maskToken(token), e.getMessage(), e);
            }
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
