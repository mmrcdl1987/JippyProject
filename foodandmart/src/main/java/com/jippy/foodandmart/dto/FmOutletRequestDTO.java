package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Why @JsonIgnoreProperties(ignoreUnknown = true):
 * The frontend and older Postman collections may send fields that were
 * removed from the data model (e.g. "zone", "approver", "subscriptionPlan").
 * Without this annotation Jackson throws UnrecognizedPropertyException and
 * the entire request fails with a 500. Silently ignoring unknown fields is
 * the correct behaviour — the removed fields are simply not persisted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutletRequestDTO {
    // ──────────────────────── outlets table ───────────────────────────────────

    @Schema(description = "Outlet Name", example = "Mehfil Restaurant")
    @NotBlank(message = "Outlet name is required")
    @Size(max = 100, message = "Outlet name must not exceed 100 characters")
    private String outletName;

    @Schema(description = "Merchant Id", example = "50")
    @NotNull(message = "Merchant ID is required")
    private Integer merchantId;
    @Schema(
            description = "Cuisine type IDs",
            example = "[1, 2, 5]"
    )
    @NotNull(message = "Cuisine type is required")
    @Size(min = 1, message = "At least one cuisine type is required")
    private Integer[] cuisineType;


    @Schema(description = "Primary outlet phone number", example = "9876543210")
    @NotBlank(message = "Outlet phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Outlet phone must be a valid 10-digit Indian mobile number")
    private String outletPhone;

    @Schema(description = "Primary outlet email address", example = "friendsrestaurant@gmail.com")
    @NotBlank(message = "Outlet email is required")
    @Email(message = "Invalid email format")
    private String outletEmail;

    @Schema(
            description = "S3 URL of the outlet image",
            example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/outlets/50/images/8d7f2c1a.jpg"
    )
    @Size(max = 1000, message = "Outlet image URL must not exceed 1000 characters")
    private String outletPicUrl;

    @Schema(description = "Alternate outlet phone number", example = "9876543211")
    @Pattern(regexp = "^[6-9]\\d{9}$",
            message = "Alternate outlet phone must be a valid 10-digit Indian mobile number")
    private String alternateOutletPhone;

    @Schema(description = "FSSAI License Number", example = "12345678901234")
    @NotBlank(message = "FSSAI Number is required")
    @Pattern(regexp = "^\\d{14}$",
            message = "FSSAI Number must contain exactly 14 digits")
    private String fssaiNumber;

    @Schema(description = "GST Number", example = "36ABCDE1234F1Z5")
    @NotBlank(message = "GST Number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST Number")
    private String gstNumber;


    @Schema(description = "Username for outlet login", example = "friends_outlet")
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50)
    private String username;

    @Schema(description = "Password for outlet login", example = "Rohan@123")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must contain uppercase, lowercase, number and special character")
    private String password;
//------------------------------------------------------------
//    outlet bank details from user bank details table
    @Schema(description = "Bank account number of the outlet", example = "123456789012")
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$",
            message = "Account number must contain 9 to 18 digits")
    private String accountNumber;

    @Schema(description = "IFSC code of the bank branch", example = "SBIN0001234")
    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC Code")
    private String ifscCode;

    @Schema(description = "Name of the bank", example = "State Bank of India")
    @NotBlank(message = "Bank name is required")
    @Size(max = 100)
    private String bankName;

    @Schema(description = "Name of the account holder", example = "John Doe")
    @NotBlank(message = "Account holder name is required")
    @Size(max = 100)
    private String accountHolderName;


    // ── address table ─────────────────────────────────────────────────────────

    @Schema(description = "Building Number", example = "10-1-20")
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @Schema(description = "Road Name", example = "Main Road")
    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @Schema(description = "Nearby Landmark", example = "Near Metro Station")
    @Size(max = 150)
    private String landmark;

    @Schema(description = "State Id", example = "2")
//    @NotNull(message = "state ID is required")
    private Integer stateId;

    @Schema(description = "City Id", example = "3")
//    @NotNull(message = "city ID is required")
    private Integer cityId;


    @Schema(description = "Area Id", example = "16")
//    @NotNull(message = "City ID is required")
    private Integer areaId;

    /*
     * Used only during bulk upload.
     * Ignored while creating a single outlet.
     */
    @Schema(description = "Area Name (Used only for bulk upload)", example = "Kukatpally")
    private String areaName;
    @Schema(description = "State Name (Used only for bulk upload)", example = "Telangana")
    private String stateName;

    // Latitude and longitude — stored to oulet_location GEOGRAPHY(POINT, 4326)
    @Schema(description = "Latitude of outlet location", example = "17.4940")
    private String latitude;

    @Schema(description = "Longitude of outlet location", example = "78.3990")
    private String longitude;

    // ── outlet_days table ─────────────────────────────────────────────────────
    @Schema(
            description = "Outlet operating days and timings",
            example = """
        [
          {
            "dayOfWeekId": 1,
            "isOpen": true,
            "openingTime": "09:00",
            "closingTime": "12:00",
          },
          {
            "dayOfWeekId": 1,
            "isOpen": true,
            "openingTime": "12:00",
            "closingTime": "22:00",
          },
           {
            "dayOfWeekId": 2,
            "isOpen": true,
            "openingTime": "12:00",
            "closingTime": "22:00",
          }
        ]
        """)
    @Valid
    private List<FmOutletDayDTO> operatingDays;

    // ── tracking ──────────────────────────────────────────────────────────────
    @Schema(description = "User who created the outlet", example = "101")
    private Integer updatedBy;

    private String uploadedBy;


}
