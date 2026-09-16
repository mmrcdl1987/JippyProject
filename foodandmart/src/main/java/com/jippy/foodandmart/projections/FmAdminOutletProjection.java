package com.jippy.foodandmart.projections;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FmAdminOutletProjection {

    // ============================================================
    // OUTLET DETAILS
    // ============================================================

    Integer getOutletId();

    String getOutletName();

    String getOutletType();

    String getOutletEmail();

    String getOutletPhone();

    String getAlternateOutletPhone();

    String getOutletPicUrl();

    // ============================================================
    // MERCHANT DETAILS
    // ============================================================

    Integer getMerchantId();

    String getMerchantName();

    // ============================================================
    // AREA DETAILS
    // ============================================================

    Integer getAreaId();

    String getAreaName();



    BigDecimal getTotalRating();

    Integer getTotalReviews();

    // ============================================================
    // STATUS
    // ============================================================

    String getIsActive();

    Boolean getIsApproved();

    // ============================================================
    // AUDIT
    // ============================================================

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
