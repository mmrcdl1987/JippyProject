package com.jippy.driver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverOutletDto {

    private Integer outletId;

    private String outletName;

    private String outletEmail;

    private Integer merchantId;

    private Integer[] cuisineType;

    private String outletPhone;

    private String alternateOutletPhone;

    private Boolean isVegOutlet;

    private Boolean isGstApplied;

    private Double radius;

    private String isActive;

    private Boolean isApproved;

    private String outletPicUrl;

    private String fssaiNumber;

    private String gstNumber;

    private String accountNumber;

    private String ifscCode;

    private String bankName;

    private String accountHolderName;

    private String buildingNumber;

    private String road;

    private String landmark;

    private Double latitude;

    private Double longitude;

}