package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverTotalEarningsProjection {

    BigDecimal getTotalPickUpCharges();

    BigDecimal getTotalDeliveryCharges();

    BigDecimal getTotalTips();

    BigDecimal getTotalSurgeFee();

    BigDecimal getTotalEarnings();

    Long getCompletedOrders();
}