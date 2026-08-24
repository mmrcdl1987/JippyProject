package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmOutletResponseDto {

    // ---------------------------------------------------------
    // Outlet Details
    // ---------------------------------------------------------

    private Integer outletId;

    private String outletName;

    private String outletEmail;

    private Integer merchantId;

    private Integer[] cuisineType;

    private String outletPhone;

    private String alternateOutletPhone;

    private BigDecimal radius;

    private String isActive;

    private Boolean isApproved;

    private String outletPicUrl;


    // ---------------------------------------------------------
    // KYC Details
    // user_kyc
    // ---------------------------------------------------------

    private String fssaiNumber;

    private String gstNumber;


    // ---------------------------------------------------------
    // Bank Details
    // user_bank_details
    // ---------------------------------------------------------

    private String accountNumber;

    private String ifscCode;

    private String bankName;

    private String accountHolderName;


    // ---------------------------------------------------------
    // Address Details
    // outlet address
    // ---------------------------------------------------------

    private String buildingNumber;

    private String road;

    private String landmark;
    // ---------------------------------------------------------
    // Outlet Location
    // oulet_location GEOGRAPHY(POINT,4326)
    // ---------------------------------------------------------

    private Double latitude;

    private Double longitude;


    // ---------------------------------------------------------
    // Operating Days
    // outlet_days
    // ---------------------------------------------------------

    private List<FmOutletDayDTO> operatingDays;
}