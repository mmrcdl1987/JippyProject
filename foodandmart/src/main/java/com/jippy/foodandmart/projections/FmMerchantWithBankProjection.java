package com.jippy.foodandmart.projections;

//gets data from DB
//To hold data coming from JOIN query (Merchant + Bank)
public interface FmMerchantWithBankProjection {

    //    for merchant basic details to fetch
    Integer getMerchantId();
    String getMerchantName();
    String getMerchantEmail();
    String getMerchantPhone();
    String getBusinessType();
//    String getStatus();

    Boolean getIsApproved();
// for merchant details
    Long getBankId();
    Long getRecipientId();
    String getAccountNumber();
    String getIfscCode();
    String getBankName();
    String getAccountHolderName();
    String getUserType();

    String getAadhaarNumberUrl();

    String getPanNumberUrl();
}

