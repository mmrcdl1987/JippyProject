package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmDriverApprovalResponseDTO {
    /*==========================================================
     = Driver Details
     ==========================================================*/

    private Integer driverId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

    private String profilePicUrl;

    /*==========================================================
     = Nominee Details
     ==========================================================*/

    private String nomineeName;

    private String nomineePhoneNumber;

    private Boolean nomineeVerified;

    /*==========================================================
     = Family Member Details
     ==========================================================*/

    private String familyMemberName;

    private String familyMemberPhoneNumber;

    private Boolean familyMemberVerified;

    /*==========================================================
     = Driver KYC Details
     ==========================================================*/

    private Integer driverKycId;

    private String aadhaarNumber;

    private String drivingLicenseNumber;

    private String rcCopy;

    private String aadharDocUrl;

    private String panDocUrl;

    private String drivingLicenseDocUrl;

    private String rcCopyDocUrl;

    /*==========================================================
     = Driver Approval Status
     ==========================================================*/

    /**
     * Indicates whether the Driver has completed
     * the approval workflow.
     *
     * false = Driver is not yet approved
     * true  = Driver is approved
     */
    private Boolean isApproved;
}
