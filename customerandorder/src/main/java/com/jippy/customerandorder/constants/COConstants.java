package com.jippy.customerandorder.constants;

import java.math.BigDecimal;

public class COConstants {

    private COConstants() {

    }

    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String STATUS_200 = "200";
    public static final String STATUS_400 = "400";
    public static final String STATUS_500 = "500";
    public static final String MSG_SUCCESS = "Success";
    public static final String MSG_ORDER_CREATED = "Order created successfully";
    public static final String ORDER_STATUS_PLACED = "PLACED";
    public static final String MSG_ORDER_ITEMS_EMPTY = "Order items cannot be empty";

    // ── Cart Messages ──────────────────────────────────────────────────────────
    public static final String MSG_CART_ADDED = "Item added to cart";
    public static final String MSG_CART_UPDATED = "Cart updated";
    public static final String MSG_CART_REMOVED = "Item removed from cart";
    public static final String MSG_INVALID_QUANTITY = "Quantity cannot be negative";
    public static final String MSG_CART_EMPTY = "Cart is empty";
    public static final String MSG_PRODUCT_FETCH_FAILED = "Unable to fetch product details";
    public static final String MSG_CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String TYPE_DRIVER = "DRIVER";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String OUTLET = "OUTLET";
    public static final String ORDER_STATUS_REJECTED = "REJECTED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_201 = "201";
    // ── Delivery Charge Settings Messages ──────────────────────────────────────
    public static final String MSG_DELIVERY_CHARGE_CREATED = "Delivery charge setting created successfully";
    public static final String MSG_INVALID_KMS_RANGE = "Invalid KMS range: 'from' must be less than 'to'";
    public static final String MSG_INVALID_UNIT_PRICE = "Unit price cannot be negative";
    public static final String MSG_DELIVERY_CHARGE_NOT_FOUND = "Delivery charge setting not found";
    public static final String MSG_DATABASE_ERROR = "Database operation failed";
    public static final String MSG_DRIVER_CHARGE_CALCULATED = "Driver charge calculated successfully";
    // ── Driver Charge Messages ──────────────────────────────────────
    public static final String MSG_PICKUP_SLAB_NOT_FOUND = "Pickup charge slab not found";
    public static final String MSG_DELIVERY_SLAB_NOT_FOUND = "Delivery charge slab not found";
    public static final String MSG_CUSTOMER_LOCATION_NOT_FOUND = "Customer location not found";
    public static final String MSG_OUTLET_LOCATION_NOT_FOUND = "Outlet location not found";
    public static final String MSG_DISTANCE_CALCULATION_FAILED = "Unable to calculate distance";
    public static final String TAX_PERCENTAGE = "5";


    // ── Driver Charge Constants ──────────────────────────────────────
    public static final String COD_LIMIT = "499";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final BigDecimal DRIVER_DEFAULT_COD_AMOUNT = BigDecimal.valueOf(1000);
    public static final boolean DRIVER_ORDERS_LOCK = true;
    public static final String MSG_ORDER_SETTINGS_NOT_FOUND = "Order settings not found";
    public static final String MSG_ORDER_SETTINGS_UPDATED = "Order settings updated successfully";
    public static final String MSG_ORDER_SETTINGS_CREATED = "Order settings created successfully";
    public static final Integer MINIMUM_POINTS_REQUIRED = 1000;
    // WALLET MESSAGES
    public static final Integer AMOUNT_PER_1000_POINTS = 100;
    public static final String WELCOME_POINTS = "WELCOME_POINTS";
    public static final String WALLET_NOT_FOUND = "Wallet not found";
    public static final String MINIMUM_POINTS_REQUIRED_MESSAGE = "Minimum 1000 points needed to convert";
    public static final String POINTS_CONVERTED_SUCCESS = "Points converted successfully";
    public static final String CUSTOMER_CREATED_SUCCESS = "Customer created successfully";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String PHONE_ALREADY_EXISTS = "Phone number already exists";
    public static final String WELCOME_POINTS_NOT_CONFIGURED = "WELCOME_POINTS not configured";
    public static final String POINTS_CONVERTED = "POINTS_CONVERTED";
    public static final String DAILY_STREAK_POINTS = "DAILY_STREAK_POINTS";
    public static final String STREAK_UPDATED = "Daily streak updated";
    public static final String STREAK_REWARD = "STREAK_REWARD";
    public static final String ALREADY_CHECKED_IN = "Already checked in today";
    public static final String STREAK_SETTINGS_NOT_FOUND = "Daily streak settings not configured";
    public static final String POINTS_TRANSFERRED = "POINTS_TRANSFERRED";
    public static final String POINTS_RECEIVED = "POINTS_RECEIVED";
    public static final String INSUFFICIENT_POINTS = "Insufficient wallet points";
    public static final String RECEIVER_NOT_FOUND = "Receiver customer not found";
    public static final String CANNOT_TRANSFER_SELF = "Cannot transfer points to same customer";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String POINTS_TRANSFER_SUCCESS = "Wallet points transferred successfully";
    // SCHEDULED ORDER
    public static final String ORDER_TYPE_NORMAL = "NORMAL";

    public static final String ORDER_TYPE_SCHEDULED_RECURRING = "SCHEDULED_RECURRING";

    public static final String ORDER_TYPE_SCHEDULED_CUSTOM_PLAN = "SCHEDULED_CUSTOM_PLAN";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "ACTIVE";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "CANCELLED";
    public static final String SUBSCRIPTION_STATUS_COMPLETED = "COMPLETED";
    public static final String SUBSCRIPTION_STATUS_PAUSED = "PAUSED";

    // MEAL TYPES
    public static final String BREAKFAST = "BREAKFAST";
    public static final String LUNCH = "LUNCH";
    public static final String DINNER = "DINNER";

    // SCHEDULED ORDER NOTIFICATIONS
    public static final String NOTIFICATION_TYPE_CREATED = "ORDER_CREATED";

    public static final String NOTIFICATION_TYPE_SCHEDULED_ORDER_CREATED = "SCHEDULED_ORDER_CREATED";

    public static final String NOTIFICATION_TYPE_SCHEDULED_ORDER_TODAY = "SCHEDULED_ORDER_TODAY";

    public static final String NOTIFICATION_TYPE_SCHEDULED_ORDER_1_HOUR_BEFORE = "SCHEDULED_ORDER_1_HOUR_BEFORE";
    // SCHEDULED ORDER MESSAGES
    public static final String MSG_SCHEDULED_TIME_REQUIRED = "Scheduled delivery time is required";
    public static final String MSG_SUBSCRIPTION_START_DATE_REQUIRED = "Subscription start date is required";
    public static final String MSG_SUBSCRIPTION_END_DATE_REQUIRED = "Subscription end date is required";
    public static final String MSG_MEAL_PREFERENCE_REQUIRED = "Meal preference is required";
    public static final String MSG_INVALID_SUBSCRIPTION_DATE = "Subscription end date must be after start date";
    public static final String MSG_TEMPLATE_ORDER_NOT_FOUND = "Template order not found";
    /*
     * SCHEDULE TYPES
     */
    public static final String MSG_SCHEDULED_ORDERS_REQUIRED = "Scheduled orders are required for custom plan";
    public static final String MSG_SUBSCRIPTION_EXPIRED = "Subscription expired";
    public static final String MSG_DUPLICATE_SCHEDULED_ORDER = "Today's scheduled order already exists";
    public static final String MSG_PRODUCT_UNAVAILABLE = "Product currently unavailable";
    public static final String MSG_SCHEDULED_ORDER_CREATED = "Scheduled orders created successfully";

    public static final String MSG_SUBSCRIPTION_DATES_REQUIRED = "Subscription dates are required";

    public static final String MSG_INVALID_SCHEDULE_DATE = "Invalid scheduled date";
    public static final String MSG_INVALID_ORDER_TYPE = "order type is invalid";
    public static final String ORDER_STATUS_OUT_FOR_DELIVERY = "out for delivery";

    public static final String ORDER_STATUS_CUSTOMER_NOT_REACHABLE = "CUSTOMER_NOT_REACHABLE";

    public static final String ORDER_STATUS_FINAL_REJECTED = "FINAL_REJECTED";

    public static final String REJECTION_TYPE_CUSTOMER = "CUSTOMER";

    public static final Integer FINAL_REJECTION_WAIT_MINUTES = 2;

    public static final Double CUSTOMER_LOCATION_RADIUS_METERS = 200.0;

    public static final String DRIVER_NOT_AT_CUSTOMER_LOCATION = "Driver not at customer location";

    public static final String GROUP_ORDER_INVITATION_ACTIVE = "ACTIVE";

    public static final String GROUP_ORDER_INVITATION_CREATED = "CREATED";

    public static final String GROUP_ORDER_INVITATION_EXPIRED = "EXPIRED";

    public static final boolean GROUP_ORDER_PLACED_FALSE = false;

    public static final boolean GROUP_ORDER_PLACED_TRUE = true;

    public static final boolean GROUP_ORDER_IS_DROPPED_TRUE = false;

    public static final String GROUP_ORDER_STATUS_LOCKED = "LOCKED";

    public static final String GROUP_ORDER_STATUS_EXPIRED = "EXPIRED";

    public static final String GROUP_ORDER_ORDER_TYPE = "GROUP_ORDER";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";

    public static final String COMMUNITY_ORDER_ORDER_TYPE = "COMMUNITY_ORDER";

    public static final String COMMUNITY_GROUP_ORDER_ORDER_TYPE = "COMMUNITY_GROUP_ORDER";

    public static final String ZONE_TYPE_COMMUNITY = "COMMUNITY";

    public  static  final String JOIN_COMMUNITY_TYPE = "JOIN";

    public  static  final String DROP_COMMUNITY_TYPE = "DROP";

    public static final String COMMUNITY_ORDER_PAYMENT_TYPE = "INDIVIDUAL";

    public static final BigDecimal HIGH_VALUE_CART_LIMIT = BigDecimal.valueOf(500);

    public static final String ITEM_ADDED_NOT_ORDERED = "ITEM_ADDED_NOT_ORDERED";

    public static final String HIGH_VALUE_CART = "HIGH_VALUE_CART";
    }

