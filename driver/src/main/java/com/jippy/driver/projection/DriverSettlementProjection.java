package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverSettlementProjection {
    Integer getDriverId();

    Long getNoOfOrdersCompleted();

    BigDecimal getTotalDriverEarnings();
}
