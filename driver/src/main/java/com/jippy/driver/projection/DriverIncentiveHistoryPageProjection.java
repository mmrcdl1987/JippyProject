package com.jippy.driver.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DriverIncentiveHistoryPageProjection {

    Integer getDriverIncentiveHistoryId();

    Integer getDriverId();

    String getDriverName();

    LocalDate getCurrDate();

    BigDecimal getIncentiveAmount();

    Integer getCompletedOrdersCount();

    LocalDateTime getCreatedAt();
}