package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for Level-1 Pending Approval Requests.
 * <p>
 * This DTO is returned by
 * GET /api/fm/getLevel1PendingApprovalRequests
 * <p>
 * Depending on entityType, only the relevant fields will be populated.
 * <p>
 * Supported Entity Types:
 * 1. OUTLET
 * 2. MERCHANT
 * 3. DRIVER
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FmLevel1PendingApprovalResponseDTO {

    /*----------------------------------------------------------
     * Approval Request Details
     *---------------------------------------------------------*/

    @Schema(description = "Approval Request Id", example = "25")
    private Integer approvalRequestId;

    @Schema(description = "Entity Type", example = "OUTLET",
            allowableValues = {"OUTLET", "MERCHANT", "DRIVER"})
    private String entityType;

    @Schema(description = "Entity Id", example = "226")
    private Integer entityId;

    @Schema(description = "Current Approval Level", example = "Level 1")
    private String currentLevel;

    @Schema(description = "Approval Status", example = "PENDING")
    private String status;

    @Schema(description = "Request Created Time", example = "2026-07-16T11:53:59")
    private LocalDateTime requestCreatedAt;


    /*---------------------------------------------------------
     * OUTLET DETAILS
     *---------------------------------------------------------*/

    @Schema(example = "226")
    private Integer outletId;

    @Schema(example = "Pepper Restaurant")
    private String outletName;

    @Schema(example = "50")
    private Integer merchantId;

    @Schema(example = "Rohan Vadluri")
    private String merchantName;

    @Schema(example = "Arabian")
    private String cuisineType;

    @Schema(example = "9876543210")
    private String outletPhone;

    @Schema(example = "pepper@gmail.com")
    private String outletEmail;

    @Schema(example = "17.385044")
    private Double latitude;

    @Schema(example = "78.486671")
    private Double longitude;

    @Schema(example = "false")
    private Boolean outletApproved;

    @Schema(example = "12345678901234")
    private String fssaiNumber;

    @Schema(example = "36ABCDE1234F1Z5")
    private String gstNumber;


    /*---------------------------------------------------------
     * MERCHANT DETAILS
     *---------------------------------------------------------*/

//    @Schema(example = "Rohan Vadluri")
//    private String merchantName; -- declared at outlet details on top

    @Schema(example = "rohan@gmail.com")
    private String merchantEmail;

    @Schema(example = "9876543215")
    private String merchantPhone;

    @Schema(example = "Restaurant")
    private String merchantBusinessType;

    @Schema(example = "false")
    private Boolean merchantApproved;

    @Schema(example = "https://jippys3bucket.s3.amazonaws.com/merchant56.png")
    private String merchantProfilePicUrl;

    @Schema(example = "234567890123")
    private String aadhaarNumber;

    @Schema(example = "ABCDE1234F")
    private String panNumber;


    /*---------------------------------------------------------
     * DRIVER DETAILS
     *---------------------------------------------------------*/

    @Schema(example = "56")
    private Integer driverId;

    @Schema(example = "Rohan")
    private String firstName;

    @Schema(example = "Vadluri")
    private String lastName;

    @Schema(example = "9876543210")
    private String phoneNumber;

    @Schema(example = "rohan@gmail.com")
    private String email;

    @Schema(example = "Ramesh")
    private String nomineeName;

    @Schema(example = "9876543211")
    private String nomineePhoneNumber;

    @Schema(example = "true")
    private Boolean nomineeVerified;

    @Schema(example = "Mahesh")
    private String familyMemberName;

    @Schema(example = "9876543222")
    private String familyMemberPhoneNumber;

    @Schema(example = "true")
    private Boolean familyMemberVerified;

    @Schema(example = "https://jippys3bucket.s3.amazonaws.com/driver56.png")
    private String profilePicUrl;

    @Schema(example = "56")
    private Integer driverKycId;

    @Schema(example = "565554614555")
    private String driverAadhaarNumber;

    @Schema(example = "TS0920200012345")
    private String drivingLicenseNumber;

    @Schema(example = "TS09EF5678")
    private String rcCopy;


    /*---------------------------------------------------------
     * ADDRESS DETAILS
     *---------------------------------------------------------*/

    @Schema(example = "221")
    private Integer addressId;

    @Schema(example = "12-5-24")
    private String buildingNumber;

    @Schema(example = "MG Road")
    private String road;

    @Schema(example = "Near RTC Bus Stand")
    private String landmark;

    @Schema(example = "Hyderabad")
    private String cityName;

    @Schema(example = "Telangana")
    private String stateName;

    @Schema(example = "KPHB")
    private String areaName;
}