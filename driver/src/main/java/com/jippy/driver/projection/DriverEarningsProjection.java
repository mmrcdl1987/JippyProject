package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverEarningsProjection {

    BigDecimal getTotalEarnings();

    Long getOrdersCount();
}