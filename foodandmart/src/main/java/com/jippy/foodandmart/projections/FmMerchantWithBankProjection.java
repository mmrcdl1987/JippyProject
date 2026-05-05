package com.jippy.foodandmart.projections;

//gets data from DB
//To hold data coming from JOIN query (Merchant + Bank)
public interface FmMerchantWithBankProjection {

    //    for merchant basic details to fetch
    Long getMerchantId();
    String getMerchantName();
    String getMerchantEmail();
    String getMerchantPhone();
    String getBusinessType();
    String getStatus();

// for merchant details
    Long getBankId();
    Long getRecipientId();
    String getAccountNumber();
    String getIfscCode();
    String getBankName();
    String getAccountHolderName();
    String getUserType();
}

