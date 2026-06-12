package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverIncentiveSettlementProjection {

    Integer getDriverId();

    BigDecimal getTotalIncentivesAmount();
}