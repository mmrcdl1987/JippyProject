package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Response DTO returned after successful outlet creation.
 * <p>
 * This DTO contains all outlet details along with address,
 * bank details and operating days.
 * <p>
 * Note:
 * Password is always returned as masked (********)
 * for security reasons.
 */
@Data
public class FmOutletCreateResponseDTO {

    // ---------------- Outlet Details ----------------

    @Schema(description = "Generated Outlet Id", example = "101")
    private Integer outletId;

    @Schema(description = "Outlet Name", example = "Mehfil Restaurant")
    private String outletName;

    @Schema(description = "Merchant Id", example = "50")
    private Integer merchantId;

    @Schema(
            description = "Outlet image URL",
            example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/outlets/50/images/abc.jpg"
    )
    private String outletPicUrl;

    @Schema(description = "Cuisine Type", example = "Indian")
    private Integer[] cuisineType;

    @Schema(description = "Primary outlet phone number", example = "9876543210")
    private String outletPhone;

    @Schema(description = "Primary outlet email address", example = "mehfil@gmail.com")
    private String outletEmail;

    @Schema(description = "Alternate outlet phone number", example = "9876543211")
    private String alternateOutletPhone;

    //    -----------------UserKyc Table -----------------
    @Schema(description = "Food Safety and Standards Authority of India (FSSAI)" +
            " License Number of the outlet.", example = "12345678901234")
    private String fssaiNumber;

    @Schema(description = "Goods and Services Tax (GST) Identification" +
            " Number (GSTIN) of the outlet.", example = "36ABCDE1234F1Z5")
    private String gstNumber;

    private Boolean isGstApplied;
//    --------------users Table----------------------

    @Schema(description = "Outlet username", example = "friends_outlet")
    private String username;

    @Schema(description = "Masked password", example = "********")
    private String password;
    // ---------------- Bank Details ----------------

    @Schema(description = "Bank Account Number", example = "123456789012")
    private String accountNumber;

    @Schema(description = "IFSC Code", example = "SBIN0001234")
    private String ifscCode;

    @Schema(description = "Bank Name", example = "State Bank of India")
    private String bankName;

    @Schema(description = "Account Holder Name", example = "Friends Restaurant")
    private String accountHolderName;

    // ---------------- Address ----------------

    @Schema(description = "Building Number", example = "10-1-20")
    private String buildingNumber;

    @Schema(description = "Road Name", example = "Main Road")
    private String road;

    @Schema(description = "Nearby Landmark", example = "Near Bus Stand")
    private String landmark;

    @Schema(description = "State Id", example = "2")
    private Integer stateId;

    @Schema(description = "City Id", example = "3")
    private Integer cityId;

    @Schema(description = "Area Id", example = "16")
    private Integer areaId;

    @Schema(description = "Latitude", example = "17.4940")
    private String latitude;

    @Schema(description = "Longitude", example = "78.3990")
    private String longitude;

    // ---------------- LIST of Operating Days ----------------

    @Schema(description = "Operating days and timings of the outlet", example = """
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

    // ---------------- Tracking ----------------

    @Schema(description = "User who created the outlet", example = "Admin")
    private Integer updatedBy;

    @Schema(description = "Outlet Active Status", example = "Y")
    private String isActive;
}