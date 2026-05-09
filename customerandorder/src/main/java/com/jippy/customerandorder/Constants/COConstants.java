package com.jippy.customerandorder.Constants;

public class COConstants {
    private COConstants(){}

    // ── Status Codes ───────────────────────────────────────────────────────────
    public static final String STATUS_200 = "200";
    public static final String STATUS_400 = "400";
    public static final String STATUS_500 = "500";

    // ── General Messages ───────────────────────────────────────────────────────
    public static final String MSG_SUCCESS = "Success";

    // ── Order Messages ─────────────────────────────────────────────────────────
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
    public static final String TYPE_DRIVER="DRIVER";
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
}
