package com.jippy.driver.projection;

import java.math.BigDecimal;

public interface DriverOrderHistoryProjection {

    Integer getDriverId();

    Integer getOrderId();

    BigDecimal getPickUpDistanceInKms();

    BigDecimal getDeliveryDistanceInKms();

    BigDecimal getPickUpCharges();

    BigDecimal getDeliverCharges();

    BigDecimal getTotalDeliveryFee();

    BigDecimal getSurgeFee();

    BigDecimal getTips();

//    from orders table
    String getOrderStatus();

    // Internal use only for fetching order history for a driver,
    // not exposed in API response
    Integer getOutletId();
}