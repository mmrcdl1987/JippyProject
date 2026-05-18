package com.jippy.customerandorder.constants;

import java.math.BigDecimal;

public class COConstants {
    public static final BigDecimal DRIVER_DEFAULT_COD_AMOUNT = new BigDecimal("500");
    public static final Boolean DRIVER_ORDERS_LOCK = true ;
    public static final String MSG_ORDER_SETTINGS_NOT_FOUND = "Order settings not found";
    public static final String MSG_ORDER_SETTINGS_UPDATED = "Order settings updated successfully";
    public static final String MSG_ORDER_SETTINGS_CREATED = "Order settings created successfully";
    public static final String MSG_CHECKOUT_SUCCESS = "Checkout successful";

    private COConstants(){}
    public static final String STATUS_200 = "200";
    public static final String STATUS_400 = "400";
    public static final String STATUS_500 = "500";
    public static final String MSG_SUCCESS = "Success";
    public static final String MSG_ORDER_CREATED = "Order created successfully";
    public static final String ORDER_STATUS_PLACED = "PLACED";
    public static final String MSG_ORDER_ITEMS_EMPTY = "Order items cannot be empty";
    public static final BigDecimal DRIVER_DEFAULT_COD_AMOUNT=BigDecimal.valueOf(1000);
    public static final boolean DRIVER_ORDERS_LOCK = true;
//    added
    public static final String MSG_ORDER_SETTINGS_NOT_FOUND = "Order settings not found";
    public static final String MSG_ORDER_SETTINGS_UPDATED = "Order settings updated successfully";
    public static final String MSG_ORDER_SETTINGS_CREATED = "Order settings created successfully";

    // ── Cart Messages ──────────────────────────────────────────────────────────
    public static final String MSG_CART_ADDED = "Item added to cart";
    public static final String MSG_CART_UPDATED = "Cart updated";
    public static final String MSG_CART_REMOVED = "Item removed from cart";
    public static final String MSG_INVALID_QUANTITY = "Quantity cannot be negative";
    public static final String MSG_CART_EMPTY = "Cart is empty";
    public static final String MSG_PRODUCT_FETCH_FAILED = "Unable to fetch product details";
    public static final String MSG_CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String TYPE_DRIVER="DRIVER";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String OUTLET = "OUTLET";
    public static final String ORDER_STATUS_REJECTED = "REJECTED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_201="201";

    // ── Delivery Charge Settings Messages ──────────────────────────────────────
    public static final String MSG_DELIVERY_CHARGE_CREATED = "Delivery charge setting created successfully";
    public static final String MSG_INVALID_KMS_RANGE = "Invalid KMS range: 'from' must be less than 'to'";
    public static final String MSG_INVALID_UNIT_PRICE = "Unit price cannot be negative";
    public static final String MSG_DELIVERY_CHARGE_NOT_FOUND = "Delivery charge setting not found";
    public static final String MSG_DATABASE_ERROR = "Database operation failed";
    // ── Driver Charge Messages ──────────────────────────────────────

    public static final String MSG_DRIVER_CHARGE_CALCULATED =
            "Driver charge calculated successfully";

    public static final String MSG_PICKUP_SLAB_NOT_FOUND =
            "Pickup charge slab not found";

    public static final String MSG_DELIVERY_SLAB_NOT_FOUND =
            "Delivery charge slab not found";

    public static final String MSG_CUSTOMER_LOCATION_NOT_FOUND =
            "Customer location not found";

    public static final String MSG_OUTLET_LOCATION_NOT_FOUND =
            "Outlet location not found";

    public static final String MSG_DISTANCE_CALCULATION_FAILED =
            "Unable to calculate distance";


// ── Driver Charge Constants ──────────────────────────────────────

    public static final String TAX_PERCENTAGE = "5";

    public static final String COD_LIMIT = "499";

    // WALLET MESSAGES

    public static final Integer MINIMUM_POINTS_REQUIRED = 1000;

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
}
