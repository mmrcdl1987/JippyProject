package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoDriverTotalEarningsProjection {

    BigDecimal getTotalPickUpCharges();

    BigDecimal getTotalDeliveryCharges();

    BigDecimal getTotalTips();

    BigDecimal getTotalSurgeFee();

    BigDecimal getTotalEarnings();

    Long getCompletedOrders();
}