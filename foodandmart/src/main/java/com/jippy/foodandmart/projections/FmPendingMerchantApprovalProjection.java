package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

public interface FmPendingMerchantApprovalProjection {

//    from merchant table
    Integer getMerchantId();

    String getMerchantName();

    String getMerchantEmail();

    String getMerchantPhone();

    String getMerchantBusinessType();

    Boolean getIsApproved();

    LocalDateTime getCreatedAt();
}
