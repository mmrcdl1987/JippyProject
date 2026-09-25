package com.jippy.notification.enums;

import java.util.Arrays;

public enum NotificationType {
    ORDER_PLACED,
    ORDER_ACCEPTED,
    FOOD_PREPARING,
    DRIVER_ASSIGNED,
    ORDER_PICKED_UP,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    NEW_ORDER,
    DELIVERY_COMPLETED,
    WALLET_CREDITED,
    WELCOME_POINTS_EARNED,
    REFERRAL_POINTS_EARNED,
    CAMPAIGN,
    PROFILE_INCOMPLETE,
    MEAL_REMINDER,
    CART_REMINDER,
    WELCOME_COUPON,
    REJECTED_ORDER,
    POINTS_CONVERTED_TO_MONEY,
    RESTAURANT_ACCEPTED,
    CUSTOMER_PROMOTIONAL_OFFER;

    public static boolean isValid(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(t -> t.name().equalsIgnoreCase(type.trim()));
    }

    public static NotificationType fromString(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        for (NotificationType t : values()) {
            if (t.name().equalsIgnoreCase(type.trim())) {
                return t;
            }
        }
        return null;
    }
}
