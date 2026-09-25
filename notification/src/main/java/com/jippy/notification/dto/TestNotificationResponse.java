package com.jippy.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNotificationResponse extends NApiResponse {
    private String firebaseMessageId;

    public TestNotificationResponse(boolean success, String message, String firebaseMessageId) {
        super(success, message);
        this.firebaseMessageId = firebaseMessageId;
    }
}
