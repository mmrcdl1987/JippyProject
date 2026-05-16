package com.jippy.foodandmart.constants;

public final class FmAppConstants {

    public static final String ROLE_OUTLET   = "ROLE_OUTLET";

    private FmAppConstants() {
    }

    // ───────────────── STATUS CODES ─────────────────

    public static final String STATUS_200 = "200";
    public static final String MSG_PRICE_UPDATED = "Prices updated successfully";
    public static final String ADDRESS_MERCHANT_TYPE = "merchant";
    public static final int DEFAULT_CREATED_BY = 1;
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_ACTIVE    = "ACTIVE";
    public static final String STATUS_INACTIVE  = "INACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED    = "FAILED";
    public static final String STATUS_SUCCESS   = "SUCCESS";

    // ── Flags ─────────────────────────────────────────────────────────────────
    public static final String FLAG_YES = "Y";
    public static final String FLAG_NO  = "N";

    // ── User Types ────────────────────────────────────────────────────────────
    public static final String TYPE_MERCHANT = "MERCHANT";
    public static final String TYPE_OUTLET   = "OUTLET";

    // ── Role IDs ──────────────────────────────────────────────────────────────
    public static final int ROLE_ID_OUTLET   = 2;
    public static final int ROLE_ID_MERCHANT = 3;

    // ── Transfer Status ───────────────────────────────────────────────────────
    public static final String TRANSFER_STATUS_COMPLETED = "COMPLETED";
    public static final String TRANSFER_STATUS_PENDING   = "PENDING";
    public static final String TRANSFER_STATUS_REJECTED  = "REJECTED";

    // ── Address Type ─────────────────────────────────────────────────────────
    public static final String ADDRESS_TYPE_OUTLET = "OUTLET";

    // ── Menu Copy Status ──────────────────────────────────────────────────────
    public static final String COPY_STATUS_SUCCESS = "SUCCESS";
    public static final String COPY_STATUS_FAILED  = "FAILED";

    public static final String  UN_APPROVED="NOT_APPROVED";




    public static final String STATUS_201 = "201";
    public static final String STATUS_404 = "404";

    // ───────────────── COMMON MESSAGES ─────────────────

    public static final String MSG_200 =
            "Request processed successfully";

    public static final String MSG_201 =
            "Created successfully";

    public static final String MSG_SUCCESS =
            "Success";

    // ───────────────── GEO CONSTANTS ─────────────────

    public static final Double DEFAULT_RADIUS_KM = 3.0;

    // ───────────────── UNAVAILABILITY SUCCESS ─────────────────

    public static final String MSG_UNAVAILABILITY_CREATED =
            "Unavailability created successfully";

    // ───────────────── UNAVAILABILITY VALIDATIONS ─────────────────

    public static final String MSG_INVALID_TYPE =
            "Invalid unavailability type";

    public static final String MSG_INVALID_DATE_RANGE =
            "Unavailability to date must be greater than from date";

    // ───────────────── UNAVAILABILITY NOT FOUND ─────────────────

    public static final String MSG_PRODUCT_NOT_FOUND =
            "Product not found";

    public static final String MSG_OUTLET_NOT_FOUND =
            "Outlet not found";

    public static final String MSG_OUTLET_CATEGORY_NOT_FOUND =
            "Outlet category not found";

    public static final String MSG_OUTLET_DAY_NOT_FOUND =
            "Outlet day not found";

    // ───────────────── UNAVAILABILITY TYPES ─────────────────

    public static final String PRODUCT =
            "PRODUCT";

    public static final String OUTLET =
            "OUTLET";

    public static final String OUTLET_CATEGORY =
            "OUTLET_CATEGORY";

    public static final String OUTLET_DAY =
            "OUTLET_DAY";
}