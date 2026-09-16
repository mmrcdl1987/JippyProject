package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

public interface FmAdminMerchantProjection {

    Integer getMerchantId();

    String getMerchantName();

    String getMerchantEmail();

    String getMerchantPhone();

    String getMerchantBusinessType();

    String getStatus();

    String getIsActive();

    Boolean getIsApproved();

    LocalDateTime getCreatedAt();

    Integer getCreatedBy();

    LocalDateTime getUpdatedAt();

    Integer getUpdatedBy();

    String getProfilePicUrl();

    Integer getAreaId();

    String getAreaName();
}
