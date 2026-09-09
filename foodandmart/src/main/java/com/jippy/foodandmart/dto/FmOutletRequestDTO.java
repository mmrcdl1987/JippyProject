package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Request DTO used for both single outlet creation and bulk outlet upload.
 *
 * Single outlet creation can use ID fields directly.
 * Bulk upload can additionally provide human-readable names, which are
 * resolved to IDs by the bulk upload service.
 *
 * FSSAI and GST are intentionally optional here because merchant bulk
 * upload does not require those values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutletRequestDTO {

    // ============================================================
    // OUTLET DETAILS
    // ============================================================

    @Schema(
            description = "Outlet Name",
            example = "Mehfil Restaurant"
    )
    @NotBlank(message = "Outlet name is required")
    @Size(
            max = 100,
            message = "Outlet name must not exceed 100 characters"
    )
    private String outletName;

    @Schema(
            description = "Merchant Id. Used for single outlet creation; "
                    + "resolved from merchantName during bulk upload.",
            example = "50"
    )
    private Integer merchantId;

    @Schema(
            description = "Merchant Name. Used during bulk upload; "
                    + "not required for single outlet creation.",
            example = "ABC Foods Pvt Ltd"
    )
    private String merchantName;

    @Schema(
            description = "Cuisine type IDs. Used for single outlet creation; "
                    + "resolved from cuisineTypeNames during bulk upload.",
            example = "[1, 2, 5]"
    )
    private Integer[] cuisineType;

    @Schema(
            description = "Cuisine type names used during bulk upload. "
                    + "Multiple cuisine names can be comma separated.",
            example = "Indian,Chinese"
    )
    private String cuisineTypeNames;

    @Schema(
            description = "Primary outlet phone number",
            example = "9876543210"
    )
    @NotBlank(message = "Outlet phone is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Outlet phone must be a valid 10-digit Indian mobile number"
    )
    private String outletPhone;

    @Schema(
            description = "Primary outlet email address",
            example = "friendsrestaurant@gmail.com"
    )
    @NotBlank(message = "Outlet email is required")
    @Email(message = "Invalid email format")
    private String outletEmail;

    @Schema(
            description = "S3 URL of the outlet image",
            example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/outlets/50/images/8d7f2c1a.jpg"
    )
    @Size(
            max = 1000,
            message = "Outlet image URL must not exceed 1000 characters"
    )
    private String outletPicUrl;

    @Schema(
            description = "Alternate outlet phone number",
            example = "9876543211"
    )
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Alternate outlet phone must be a valid 10-digit Indian mobile number"
    )
    private String alternateOutletPhone;

    // ============================================================
    // VEG / GST
    // ============================================================

    @Schema(
            description = "Whether the outlet is a vegetarian outlet",
            example = "true"
    )
    private Boolean isVegOutlet;

    @Schema(
            description = "Whether GST is applied for the outlet",
            example = "false"
    )
    private Boolean isGstApplied;

    // ============================================================
    // OUTLET KYC
    // ============================================================

    @Schema(
            description = "Aadhaar Number for the outlet owner",
            example = "123456789012"
    )
    @Pattern(
            regexp = "^$|^[2-9]{1}[0-9]{11}$",
            message = "Aadhaar must be a valid 12-digit number"
    )
    private String aadharNumber;

    @Schema(
            description = "PAN Number for the outlet owner",
            example = "ABCDE1234F"
    )
    @Pattern(
            regexp = "^$|^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "PAN must be in format: AAAAA9999A"
    )
    private String panNumber;

    /**
     * FSSAI is optional for merchant bulk upload.
     */
    @Schema(
            description = "FSSAI License Number. Optional during merchant bulk upload.",
            example = "12345678901234"
    )
    @Pattern(
            regexp = "^$|^\\d{14}$",
            message = "FSSAI Number must contain exactly 14 digits when provided"
    )
    private String fssaiNumber;

    /**
     * GST is optional for merchant bulk upload.
     */
    @Schema(
            description = "GST Number. Optional during merchant bulk upload.",
            example = "36ABCDE1234F1Z5"
    )
    @Pattern(
            regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST Number"
    )
    private String gstNumber;

    // ============================================================
    // OUTLET LOGIN
    // ============================================================

    /**
     * Required for normal single-outlet creation.
     *
     * Bulk upload may generate username before service validation when
     * the CSV/Excel value is blank.
     */
    @Schema(
            description = "Outlet login username. "
                    + "For bulk upload, blank values can be generated automatically.",
            example = "friends_outlet"
    )
    @NotBlank(message = "Username is required")
    @Size(
            min = 4,
            max = 50,
            message = "Username must be between 4 and 50 characters"
    )
    private String username;

    /**
     * Required for normal single-outlet creation.
     *
     * Bulk upload may generate password before service validation when
     * the CSV/Excel value is blank.
     */
    @Schema(
            description = "Outlet login password. "
                    + "For bulk upload, blank values can be generated automatically.",
            example = "Rohan@123"
    )
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    // ============================================================
    // OUTLET BANK DETAILS
    // ============================================================

    @Schema(
            description = "Bank account number of the outlet",
            example = "123456789012"
    )
    @NotBlank(message = "Account number is required")
    @Pattern(
            regexp = "^[0-9]{9,18}$",
            message = "Account number must contain 9 to 18 digits"
    )
    private String accountNumber;

    @Schema(
            description = "IFSC code of the bank branch",
            example = "SBIN0001234"
    )
    @NotBlank(message = "IFSC code is required")
    @Pattern(
            regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC Code"
    )
    private String ifscCode;

    @Schema(
            description = "Name of the bank",
            example = "State Bank of India"
    )
    @NotBlank(message = "Bank name is required")
    @Size(
            max = 100,
            message = "Bank name must not exceed 100 characters"
    )
    private String bankName;

    @Schema(
            description = "Name of the account holder",
            example = "John Doe"
    )
    @NotBlank(message = "Account holder name is required")
    @Size(
            max = 150,
            message = "Account holder name must not exceed 150 characters"
    )
    private String accountHolderName;

    // ============================================================
    // OUTLET ADDRESS
    // ============================================================

    @Schema(
            description = "Building Number",
            example = "10-1-20"
    )
    @NotBlank(message = "Building number is required")
    @Size(
            max = 500,
            message = "Building number must not exceed 500 characters"
    )
    private String buildingNumber;

    @Schema(
            description = "Road Name",
            example = "Main Road"
    )
    @NotBlank(message = "Road is required")
    @Size(
            max = 100,
            message = "Road must not exceed 100 characters"
    )
    private String road;

    @Schema(
            description = "Nearby Landmark",
            example = "Near Metro Station"
    )
    @Size(
            max = 150,
            message = "Landmark must not exceed 150 characters"
    )
    private String landmark;

    // ============================================================
    // LOCATION IDS
    // ============================================================

    /**
     * IDs are primarily used for single outlet creation.
     * Bulk upload resolves names into these IDs.
     */
    @Schema(
            description = "State Id",
            example = "2"
    )
    private Integer stateId;

    @Schema(
            description = "City Id",
            example = "3"
    )
    private Integer cityId;

    @Schema(
            description = "Area Id",
            example = "16"
    )
    private Integer areaId;

    // ============================================================
    // LOCATION NAMES - BULK UPLOAD
    // ============================================================

    @Schema(
            description = "Area Name used during bulk upload",
            example = "Kukatpally"
    )
    private String areaName;

    @Schema(
            description = "State Name used during bulk upload",
            example = "Telangana"
    )
    private String stateName;

    @Schema(
            description = "City Name used during bulk upload",
            example = "Hyderabad"
    )
    private String cityName;

    // ============================================================
    // LOCATION
    // ============================================================

    @Schema(
            description = "Latitude of outlet location",
            example = "17.4940"
    )
    private String latitude;

    @Schema(
            description = "Longitude of outlet location",
            example = "78.3990"
    )
    private String longitude;

    // ============================================================
    // OPERATING DAYS / MULTIPLE TIMINGS
    // ============================================================

    @Schema(
            description = "Outlet operating days and timings",
            example = """
                    [
                      {
                        "dayOfWeekId": 1,
                        "isOpen": true,
                        "openingTime": "09:00",
                        "closingTime": "12:00"
                      },
                      {
                        "dayOfWeekId": 1,
                        "isOpen": true,
                        "openingTime": "12:00",
                        "closingTime": "22:00"
                      },
                      {
                        "dayOfWeekId": 2,
                        "isOpen": true,
                        "openingTime": "12:00",
                        "closingTime": "22:00"
                      }
                    ]
                    """
    )
    @Valid
    private List<FmOutletDayDTO> operatingDays;

    // ============================================================
    // TRACKING
    // ============================================================

    @Schema(
            description = "User who created or updated the outlet",
            example = "101"
    )
    private Integer updatedBy;

    @Schema(
            description = "User/source who uploaded the bulk file",
            example = "Admin"
    )
    private String uploadedBy;
}
