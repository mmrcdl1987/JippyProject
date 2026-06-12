package com.jippy.driver.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DriverIncentiveDetailProjection {

    Integer getDriverId();

    LocalDate getCurrDate();

    BigDecimal getIncentiveAmount();

    Integer getCompletedOrdersCount();
}