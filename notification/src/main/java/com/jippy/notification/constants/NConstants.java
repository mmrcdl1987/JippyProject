package com.jippy.notification.constants;

public class NConstants {

    public static final String ROLE_OUTLET = "OUTLET";
    public static final String TOPIC_PREFIX = "outlet_";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    public static final String ROLE_DRIVER = "DRIVER";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    public static final String SUBJECT_NEW_ORDER =
            "ORDER_CREATED";

    public static final String SUBJECT_REJECTED_ORDER =
            "REJECTED_ORDER";

    public static final String SUBJECT_SCHEDULED_ORDER_CREATED = "SCHEDULED_ORDER_CREATED";
    public static final String SUBJECT_SCHEDULED_ORDER_TODAY = "SCHEDULED_ORDER_TODAY";
    public static final String SUBJECT_SCHEDULED_ORDER_1_HOUR_BEFORE = "SCHEDULED_ORDER_1_HOUR_BEFORE";
    /*
     * NOTIFICATION TYPES
     */
    public static final String NOTIFICATION_TYPE_CREATED = "SCHEDULED_ORDER_CREATED";
    public static final String NOTIFICATION_TYPE_TODAY = "SCHEDULED_ORDER_TODAY";
    public static final String NOTIFICATION_TYPE_ONE_HOUR = "SCHEDULED_ORDER_1_HOUR_BEFORE";
    public static final String SUBJECT_PROFILE_INCOMPLETE = "PROFILE_INCOMPLETE";
    public static final String REFERENCE_TYPE_CUSTOMER = "CUSTOMER";

    public static final String REFERENCE_TYPE_CART = "CART";

    public static final String NOTIFICATION_STATUS_PENDING = "PENDING";

    public static final String NOTIFICATION_STATUS_SENT = "SENT";

    public static final String MEAL_REMINDER = "MEAL_REMINDER";

    public static final String REFERENCE_TYPE_MEAL_REMINDER = "MEAL_REMINDER";
    private NConstants() {
    }


}