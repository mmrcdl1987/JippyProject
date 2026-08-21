package com.jippy.division.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DivActiveDiscountsProjection {

    Integer getPromotionScheduleId();
    Integer getOutletId();
    Integer getProductId();
    String getSourceType();
    Integer getSourceId();
    BigDecimal getMinOrderValue();
    BigDecimal getDiscountAmount();
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();
    String getPriceType();
    Integer getUsageLimitPerUser();
    String getCouponCode();
    String getpriceModelName();
    String getMealTypeSlotIdsStr();
    String getPromotionMessage();
    Integer getMaxSelection();
}
