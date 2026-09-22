package com.jippy.foodandmart.projections;

//gets data from DB
//To hold data coming from JOIN query (Merchant + Bank + Address + KYC)
public interface FmMerchantWithBankProjection {

    //    for merchant basic details to fetch
    Integer getMerchantId();
    String getMerchantName();
    String getMerchantEmail();
    String getMerchantPhone();
    String getBusinessType();
//    String getStatus();

    Boolean getIsApproved();

    // for merchant address details
    String getBuildingNumber();
    String getRoad();
    String getLandmark();
    Integer getStateId();
    String getStateName();
    Integer getCityId();
    String getCityName();
    Integer getAreaId();
    String getAreaName();

    // for merchant bank details
    Long getBankId();
    Long getRecipientId();
    String getAccountNumber();
    String getIfscCode();
    String getBankName();
    String getAccountHolderName();
    String getUserType();

    // for KYC numbers
    String getAadharNumber();
    String getPanNumber();

    // for KYC document URLs
    String getAadhaarNumberUrl();
    String getPanNumberUrl();
}
