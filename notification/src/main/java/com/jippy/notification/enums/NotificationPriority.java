package com.jippy.notification.enums;

import java.util.Arrays;

public enum NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static boolean isValid(String priority) {
        if (priority == null || priority.isBlank()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(p -> p.name().equalsIgnoreCase(priority.trim()));
    }

    public static NotificationPriority fromString(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        for (NotificationPriority p : values()) {
            if (p.name().equalsIgnoreCase(priority.trim())) {
                return p;
            }
        }
        return null;
    }
}
