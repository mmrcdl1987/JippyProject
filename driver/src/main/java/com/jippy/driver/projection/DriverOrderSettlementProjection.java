package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverOrderSettlementProjection {
    Integer getDriverId();

    String getOrderId();

    BigDecimal getPickUpCharges();

    BigDecimal getDeliverCharges();

    BigDecimal getTotalDeliveryFee();

    BigDecimal getSurgeFee();

    BigDecimal getTips();
}
