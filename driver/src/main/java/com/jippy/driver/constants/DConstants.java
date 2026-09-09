package com.jippy.driver.constants;

import java.math.BigDecimal;

public final class DConstants {

    public static final Boolean STATUS_FALSE = false;

    // ============================================================
    // COMMON STATUS CONSTANTS
    // ============================================================
    public static final String STATUS_200 = "200";
    public static final String STATUS_201 = "201";
    public static final String STATUS_400 = "400";
    public static final String STATUS_500 = "500";
    public static final String TYPE_OUTLET="OUTLET";

    public static final String MSG_SUCCESS = "SUCCESS";


    // ============================================================
    // ROLES
    // ============================================================

    public static final String ROLE_DRIVER = "ROLE_DRIVER";
    public static final String CURRENT_MONTH = "CURRENT_MONTH";


    // ============================================================
    // COMMON FILTER / MONTH CONSTANTS
    // ============================================================
    public static final String current_Month = "currentMonth";
    public static final String ALL = "ALL";
    public static final String AWS_PROFILE_PIC_STATIC_URL =
            "https://jippys3bucket.s3.ap-south-2.amazonaws.com/";


    // ============================================================
    // AWS
    // ============================================================
    public static final String TYPE_DRIVER = "DRIVER";


    // ============================================================
    // USER TYPES
    // ============================================================
    public static final String TYPE_CUSTOMER = "CUSTOMER";
    public static final String TYPE_MERCHANT = "MERCHANT";
    public static final String TransactionType_debit = "debit";


    // ============================================================
    // TRANSACTION TYPES
    // ============================================================
    public static final String TransactionType_credit = "credit";
    public static final String MSG_DELIVERY_CHARGE_CREATED =
            "Delivery charge setting created successfully";


    // ============================================================
    // DELIVERY CHARGE SETTINGS MESSAGES
    // ============================================================
    public static final String MSG_INVALID_KMS_RANGE =
            "Invalid KMS range: 'from' must be less than 'to'";
    public static final String MSG_INVALID_UNIT_PRICE =
            "Unit price cannot be negative";
    public static final String MSG_DELIVERY_CHARGE_NOT_FOUND =
            "Delivery charge setting not found";
    public static final String MSG_DATABASE_ERROR =
            "Database operation failed";
    public static final String MSG_DRIVER_CHARGE_CALCULATED =
            "Driver charge calculated successfully";


    // ============================================================
    // DRIVER CHARGE MESSAGES
    // ============================================================
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
    public static final String TAX_PERCENTAGE = "5";


    // ============================================================
    // DRIVER CHARGE CONSTANTS
    // ============================================================
    public static final String COD_LIMIT = "499";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final BigDecimal DRIVER_DEFAULT_COD_AMOUNT =
            BigDecimal.valueOf(1000);
    public static final boolean DRIVER_ORDERS_LOCK = true;
    public static final String MSG_ORDER_SETTINGS_NOT_FOUND =
            "Order settings not found";


    // ============================================================
    // ORDER SETTINGS MESSAGES
    // ============================================================
    public static final String MSG_ORDER_SETTINGS_UPDATED =
            "Order settings updated successfully";
    public static final String MSG_ORDER_SETTINGS_CREATED =
            "Order settings created successfully";
    public static final String COMMUNITY_TYPE = "COMMUNITY";


    // ============================================================
    // COMMUNITY
    // ============================================================
    public static final String FILTER_ALL = "ALL";


    // ============================================================
    // INCENTIVE HISTORY FILTERS
    // ============================================================
    public static final String FILTER_DAILY = "DAILY";
    public static final String FILTER_WEEKLY = "WEEKLY";
    public static final String FILTER_MONTHLY = "MONTHLY";
    public static final int DEFAULT_PAGE = 0;


    // ============================================================
    // PAGINATION
    // ============================================================
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String INVALID_INCENTIVE_HISTORY_FILTER =
            "Invalid filter: ";


    // ============================================================
    // INCENTIVE HISTORY VALIDATION
    // ============================================================
    public static final String INVALID_DATE_RANGE =
            "startDate cannot be greater than endDate";
    public static final String SORT_BY_CURR_DATE = "currDate";


    // ============================================================
    // INCENTIVE HISTORY SORTING
    // ============================================================
    public static final String SORT_BY_HISTORY_ID =
            "driverIncentiveHistoryId";
    public static final String FROM_EMAIL =
            "accounts@jippymart.in";


    // ============================================================
    // EMAIL
    // ============================================================

    private DConstants() {
        // Utility class
    }
}