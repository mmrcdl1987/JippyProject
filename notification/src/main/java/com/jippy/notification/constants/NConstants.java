package com.jippy.notification.constants;

public class NConstants {

    private NConstants() {
    }

    public static final String ROLE_OUTLET = "OUTLET";
    public static final String MSG_NEW_ORDER = "New order received";

    public static final String TOPIC_PREFIX = "outlet_";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    public static final String ROLE_DRIVER = "DRIVER";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String SUBJECT_NEW_ORDER = "NEW ORDER NOTIFICATION";
    public static final String SUBJECT_REJECTED_ORDER =
            "REJECTED ORDER NOTIFICATION";
    public static final String MSG_REJECTED_ORDER =
            "Order reassigned to specialized outlet";


}