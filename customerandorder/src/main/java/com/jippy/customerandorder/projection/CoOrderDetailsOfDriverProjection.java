package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOrderDetailsOfDriverProjection {

    String getOrderId();

    Integer getOutletId();

    String getCustomerName();

    Integer getDriverId();

    String getOrderStatus();

    BigDecimal getPickUpDistanceInKms();

    BigDecimal getDeliveryDistanceInKms();

    BigDecimal getPickUpCharges();

    BigDecimal getDriverDeliveryFee();

    BigDecimal getDriverTotalCharges();
}