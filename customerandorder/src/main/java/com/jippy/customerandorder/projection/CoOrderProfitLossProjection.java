package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOrderProfitLossProjection {

    String getOrderId();
    Integer getOutletId();
    BigDecimal getRevenue();
    BigDecimal getMerchantTotalPrice();
    BigDecimal getDriverDeliveryFee();
    BigDecimal getDiscountAmount();
    String getDiscountType();
}