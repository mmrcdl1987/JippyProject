package com.jippy.notification.enums;

import java.util.Arrays;

public enum NotificationRole {
    CUSTOMER,
    DRIVER,
    OUTLET,
    MERCHANT,
    ADMIN;

    public static boolean isValid(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(r -> r.name().equalsIgnoreCase(role.trim()));
    }

    public static NotificationRole fromString(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        for (NotificationRole r : values()) {
            if (r.name().equalsIgnoreCase(role.trim())) {
                return r;
            }
        }
        return null;
    }
}
