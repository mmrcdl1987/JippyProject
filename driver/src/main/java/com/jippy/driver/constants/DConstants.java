package com.jippy.driver.constants;

import java.math.BigDecimal;

public class DConstants {

    private DConstants(){}
    public static final Boolean STATUS_FALSE = false;
    public static final String STATUS_200 = "200";
    public static final String STATUS_400 = "400";
    public static final String STATUS_500 = "500";
    public static final String MSG_SUCCESS = "Success";
    public static final String ROLE_DRIVER ="ROLE_DRIVER";

    public static final String AWS_PROFILE_PIC_STATIC_URL ="https://jippys3bucket.s3.ap-south-2.amazonaws.com/";
    public static final String TYPE_DRIVER="DRIVER";
    public static final String TYPE_CUSTOMER = "CUSTOMER";
    public static final String TYPE_MERCHANT = "MERCHANT";
    public static final String STATUS_201="201";
    public static final String TransactionType_debit="debit";
    public static final String TransactionType_credit="credit";

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

    public static final String STATUS_DELIVERED = "DELIVERED";

    public static final BigDecimal DRIVER_DEFAULT_COD_AMOUNT=BigDecimal.valueOf(1000);
    public static final boolean DRIVER_ORDERS_LOCK = true;
    public static final String MSG_ORDER_SETTINGS_NOT_FOUND = "Order settings not found";
    public static final String MSG_ORDER_SETTINGS_UPDATED = "Order settings updated successfully";
    public static final String MSG_ORDER_SETTINGS_CREATED = "Order settings created successfully";

    public static final String COMMUNITY_TYPE = "COMMUNITY";
}
