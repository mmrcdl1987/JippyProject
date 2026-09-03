package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used to update outlet details.
 *
 * Note:
 * Username and Password cannot be updated
 * through this API.
 */
//If the request JSON contains extra fields that are
// not present in the DTO, ignore them instead of throwing an error
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmUpdateOutletRequestDTO {

    // ------------------------------------------------------------------
    // Outlet Details
    // ------------------------------------------------------------------

    @Schema(description = "Outlet Name", example = "Friends Restaurant")
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

    @Schema(description = "Outlet Email", example = "friendsrestaurant@gmail.com")
    @NotBlank(message = "Outlet email is required")
    @Email(message = "Invalid email format")
    private String outletEmail;

    @Schema(description = "Outlet Phone Number", example = "9876543210")
    @NotBlank(message = "Outlet phone is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Outlet phone must be a valid 10-digit Indian mobile number")
    private String outletPhone;

    @Schema(description = "Alternate Outlet Phone", example = "9876543211")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Alternate outlet phone must be a valid 10-digit Indian mobile number")
    private String alternateOutletPhone;

    private Boolean isGstApplied;

    // ------------------------------------------------------------------
    // Bank Details
    // ------------------------------------------------------------------

    @Schema(description = "Bank Account Number", example = "123456789012")
    @NotBlank(message = "Account number is required")
    @Pattern(
            regexp = "^[0-9]{9,18}$",
            message = "Account number must contain 9 to 18 digits")
    private String accountNumber;

    @Schema(description = "IFSC Code", example = "SBIN0001234")
    @NotBlank(message = "IFSC code is required")
    @Pattern(
            regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC Code")
    private String ifscCode;

    @Schema(description = "Bank Name", example = "State Bank of India")
    @NotBlank(message = "Bank name is required")
    @Size(max = 100)
    private String bankName;

    @Schema(description = "Account Holder Name", example = "Friends Restaurant")
    @NotBlank(message = "Account holder name is required")
    @Size(max = 100)
    private String accountHolderName;

    // ------------------------------------------------------------------
    // Address Details
    // ------------------------------------------------------------------

    @Schema(description = "Aadhaar Number", example = "123456789012")
    @Pattern(
            regexp = "^$|^[2-9]{1}[0-9]{11}$",
            message = "Aadhaar must be a valid 12-digit number")
    private String aadharNumber;

    @Schema(description = "Aadhaar Number URL")
    @Size(max = 500)
    private String aadhaarNumberUrl;

    @Schema(description = "PAN Number", example = "ABCDE1234F")
    @Pattern(
            regexp = "^$|^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "PAN must be in format: AAAAA9999A")
    private String panNumber;

    @Schema(description = "PAN Number URL")
    @Size(max = 500)
    private String panNumberUrl;

    @Schema(description = "FSSAI Number", example = "12345678901234")
    @Pattern(
            regexp = "^$|^\\d{14}$",
            message = "FSSAI Number must contain exactly 14 digits when provided")
    private String fssaiNumber;

    @Schema(description = "FSSAI Number URL")
    @Size(max = 500)
    private String fssaiNumberUrl;

    @Schema(description = "GST Number", example = "36ABCDE1234F1Z5")
    @Pattern(
            regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST Number")
    private String gstNumber;

    @Schema(description = "GST Number URL")
    @Size(max = 500)
    private String gstNumberUrl;

    @Schema(description = "Building Number", example = "10-1-20")
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @Schema(description = "Road", example = "Main Road")
    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @Schema(description = "Landmark", example = "Near Metro Station")
    @Size(max = 150)
    private String landmark;

    @Schema(description = "State Id", example = "2")
    private Integer stateId;

    @Schema(description = "City Id", example = "3")
    private Integer cityId;

    @Schema(description = "Area Id", example = "13")
    private Integer areaId;

    // ------------------------------------------------------------------
    // Outlet Location
    // ------------------------------------------------------------------

    @Schema(description = "Latitude", example = "17.4940")
    private String latitude;

    @Schema(description = "Longitude", example = "78.3990")
    private String longitude;

    // ------------------------------------------------------------------
    // Operating Days
    // ------------------------------------------------------------------

    @Valid
    @Schema(
            description = "Operating days and timings of the outlet",
            example = """
            [
              {
                "dayOfWeekId": 1,
                "isOpen": true,
                "openingTime": "09:00",
                "closingTime": "22:00",
                "slotType": "FULL_DAY"
              },
              {
                "dayOfWeekId": 2,
                "isOpen": true,
                "openingTime": "09:00",
                "closingTime": "22:00",
                "slotType": "FULL_DAY"
              }
            ]
            """)
    private List<FmOutletDayDTO> operatingDays;

    // ------------------------------------------------------------------
    // Tracking
    // ------------------------------------------------------------------

    @Schema(description = "Updated By", example = "101")
    private Integer updatedBy;
}