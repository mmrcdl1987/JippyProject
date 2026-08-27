package com.jippy.foodandmart.constants;

public final class FmAppConstants {

    private FmAppConstants() {
    }

    // ============================================================
    // COMMON STATUS
    // ============================================================

    public static final Boolean STATUS_FALSE = false;

    public static final String STATUS_200 = "200";
    public static final String STATUS_201 = "201";
    public static final String STATUS_404 = "404";

    public static final String MSG_200 =
            "Request processed successfully";

    public static final String MSG_201 =
            "Created successfully";

    public static final String MSG_SUCCESS =
            "Success";


    // ============================================================
    // APPROVAL REQUEST
    // ============================================================

    public static final String DEACTIVATE_DRIVER = "N";

    public static final String APPROVAL_LEVEL_1 = "Level 1";
    public static final String APPROVAL_LEVEL_2 = "Level 2";
    public static final String APPROVAL_LEVEL_3 = "Level 3";

    public static final String APPROVAL_STATUS_AUTO_APPROVED =
            "AUTO_APPROVED";

    public static final String APPROVAL_STATUS_PENDING =
            "PENDING";

    public static final String APPROVAL_STATUS_APPROVED =
            "APPROVED";

    public static final String APPROVAL_STATUS_REJECTED =
            "REJECTED";

    public static final Integer SYSTEM_APPROVER_ID = 0;


    // ============================================================
    // APPROVAL RESPONSE MESSAGES
    // ============================================================

    public static final String MSG_APPROVAL_REQUEST_UPDATED =
            "Approval Requests Updated Successfully.";

    public static final String MSG_APPROVAL_REQUEST_UPDATED_SUCCESS =
            "Approval Requests Updated Successfully.";

    public static final String MSG_REJECTED_REASON_REQUIRED =
            "Rejected Reason is mandatory when Status is REJECTED.";

    public static final String MSG_REJECTED_REASON_NOT_ALLOWED =
            "Rejected Reason must be empty when Status is APPROVED.";

    public static final String MSG_APPROVER_NOT_FOUND =
            "Approver Not Found : ";

    public static final String MSG_APPROVAL_REQUEST_NOT_FOUND =
            "Approval Request IDs not found : ";

    public static final String MSG_DUPLICATE_APPROVAL_REQUEST =
            "Duplicate Approval Request ID Found : ";

    public static final String MSG_UNSUPPORTED_ENTITY_TYPE =
            "Unsupported Entity Type : ";


    // ============================================================
    // APPROVAL WORKFLOW TYPES
    // ============================================================

    public static final String WORKFLOW_TYPE_CASCADE =
            "CASCADE";

    public static final String WORKFLOW_TYPE_PARALLEL =
            "PARALLEL";


    // ============================================================
    // ROLES
    // ============================================================

    public static final String ROLE_OUTLET =
            "ROLE_OUTLET";

    public static final String ROLE_MERCHANT =
            "ROLE_MERCHANT";

    public static final String ROLE_DRIVER =
            "ROLE_DRIVER";

    public static final String ROLE_CUSTOMER =
            "ROLE_CUSTOMER";


    // ============================================================
    // ROLE IDs
    // ============================================================

    public static final int ROLE_ID_OUTLET = 2;

    public static final int ROLE_ID_MERCHANT = 3;


    // ============================================================
    // FORGOT PASSWORD
    // ============================================================

    public static final String FORGOT_PASSWORD_OTP =
            "FORGOT_PASSWORD_OTP_";


    // ============================================================
    // PRICE
    // ============================================================

    public static final String MSG_PRICE_UPDATED =
            "Prices updated successfully";


    // ============================================================
    // DEFAULT VALUES
    // ============================================================

    public static final int DEFAULT_CREATED_BY = 1;


    // ============================================================
    // GENERAL STATUS VALUES
    // ============================================================

    public static final String STATUS_PENDING =
            "PENDING";

    public static final String STATUS_ACTIVE =
            "ACTIVE";

    public static final String STATUS_INACTIVE =
            "INACTIVE";

    public static final String STATUS_COMPLETED =
            "COMPLETED";

    public static final String STATUS_FAILED =
            "FAILED";

    public static final String STATUS_SUCCESS =
            "SUCCESS";


    // ============================================================
    // FLAGS
    // ============================================================

    public static final String FLAG_YES = "Y";

    public static final String FLAG_NO = "N";


    // ============================================================
    // USER TYPES
    // ============================================================

    public static final String TYPE_MERCHANT =
            "MERCHANT";

    public static final String TYPE_OUTLET =
            "OUTLET";

    public static final String TYPE_DRIVER =
            "DRIVER";

    public static final String TYPE_CUSTOMER =
            "CUSTOMER";

    public static final String TYPE_PRODUCT =
            "PRODUCT";

    public static final String TYPE_EMPLOYEE =
            "EMPLOYEE";


    // ============================================================
    // PRODUCT TYPES
    // ============================================================

    public static final String PRODUCT_TYPE_PRODUCT =
            "PRODUCT";

    public static final String PRODUCT_TYPE_MASTER_PRODUCT =
            "MASTERPRODUCT";


    // ============================================================
    // CATEGORY TYPES
    // ============================================================

    public static final String CATEGORY_TYPE_ALL =
            "ALL";

    public static final String CATEGORY_TYPE_HOME =
            "HOME";


    // ============================================================
    // TRANSFER STATUS
    // ============================================================

    public static final String TRANSFER_STATUS_COMPLETED =
            "COMPLETED";

    public static final String TRANSFER_STATUS_PENDING =
            "PENDING";

    public static final String TRANSFER_STATUS_REJECTED =
            "REJECTED";


    // ============================================================
    // ADDRESS TYPES
    // ============================================================

    public static final String ADDRESS_TYPE_OUTLET =
            "OUTLET";

    public static final String ADDRESS_MERCHANT_TYPE =
            "merchant";


    // ============================================================
    // APPROVAL
    // ============================================================

    public static final String UN_APPROVED =
            "NOT_APPROVED";


    // ============================================================
    // GEO CONSTANTS
    // ============================================================

    public static final Double DEFAULT_RADIUS_KM =
            3.0;

    public static final Double NEARBY_OUTLET_RADIUS_METERS =
            5000.0;


    // ============================================================
    // UNAVAILABILITY - SUCCESS
    // ============================================================

    public static final String MSG_UNAVAILABILITY_CREATED =
            "Unavailability created successfully";


    // ============================================================
    // UNAVAILABILITY - VALIDATION
    // ============================================================

    public static final String MSG_INVALID_TYPE =
            "Invalid unavailability type";

    public static final String MSG_INVALID_DATE_RANGE =
            "Unavailability to date must be greater than from date";


    // ============================================================
    // UNAVAILABILITY - NOT FOUND
    // ============================================================

    public static final String MSG_PRODUCT_NOT_FOUND =
            "Product not found";

    public static final String MSG_OUTLET_NOT_FOUND =
            "Outlet not found";

    public static final String MSG_OUTLET_CATEGORY_NOT_FOUND =
            "Outlet category not found";

    public static final String MSG_OUTLET_DAY_NOT_FOUND =
            "Outlet day not found";


    // ============================================================
    // UNAVAILABILITY TYPES
    // ============================================================

    public static final String PRODUCT =
            "PRODUCT";

    public static final String OUTLET =
            "OUTLET";

    public static final String OUTLET_CATEGORY =
            "OUTLET_CATEGORY";

    public static final String OUTLET_DAY =
            "OUTLET_DAY";


    // ============================================================
    // REVIEWS
    // ============================================================

    public static final String REVIEW_TYPE_OUTLET =
            "Outlet";

    public static final String REVIEW_TYPE_DRIVER =
            "Driver";

    public static final String MSG_REVIEW_SAVED =
            "Review saved successfully";

    public static final String MSG_REVIEW_NOT_FOUND =
            "Review not found";

    public static final String MSG_INVALID_REVIEW_TYPE =
            "Review type must be Outlet or Driver";

    public static final String MSG_DRIVER_NOT_FOUND =
            "Driver not found";

    public static final String MSG_CUSTOMER_NOT_FOUND =
            "Customer not found";


    // ============================================================
    // EMAIL
    // ============================================================

    public static final String FROM_EMAIL =
            "accounts@jippymart.in";

    public static final String FROM_EMAIL_NAME =
            "Jippy";

    public static final Integer EMAIL_OTP_EXPIRY_MINUTES =
            10;


    // ============================================================
    // CATEGORY IMAGE VALIDATION
    // ============================================================

    public static final long CATEGORY_IMAGE_MAX_SIZE =
            5L * 1024L * 1024L; // 5 MB

    public static final int CATEGORY_IMAGE_MIN_WIDTH =
            200;

    public static final int CATEGORY_IMAGE_MIN_HEIGHT =
            200;

    public static final int CATEGORY_IMAGE_MAX_WIDTH =
            5000;

    public static final int CATEGORY_IMAGE_MAX_HEIGHT =
            5000;

    public static final double CATEGORY_IMAGE_MIN_ASPECT_RATIO =
            0.25;

    public static final double CATEGORY_IMAGE_MAX_ASPECT_RATIO =
            4.0;


    // ============================================================
    // CATEGORY IMAGE CONTENT TYPES
    // ============================================================

    public static final String IMAGE_CONTENT_TYPE_JPEG =
            "image/jpeg";

    public static final String IMAGE_CONTENT_TYPE_PNG =
            "image/png";

    public static final String IMAGE_CONTENT_TYPE_WEBP =
            "image/webp";


    // ============================================================
    // CATEGORY IMAGE EXTENSIONS
    // ============================================================

    public static final String IMAGE_EXTENSION_JPG =
            ".jpg";

    public static final String IMAGE_EXTENSION_JPEG =
            ".jpeg";

    public static final String IMAGE_EXTENSION_PNG =
            ".png";

    public static final String IMAGE_EXTENSION_WEBP =
            ".webp";


    // ============================================================
    // CATEGORY IMAGE S3
    // ============================================================

    public static final String CATEGORY_IMAGE_S3_FOLDER =
            "food-mart/categories/";
}