package com.jippy.division.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ActiveCouponProjection {

    Integer getSourceId();

    String getCouponCode();

    BigDecimal getDiscountValue();

    BigDecimal getMinimumOrderValue();

    LocalDateTime getStartDateTime();

    LocalDateTime getEndDateTime();

    String getDiscountType();
}