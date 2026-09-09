package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOrderDetailsOfOutletProjection {

    String getOrderId();

    Integer getOutletId();

    Integer getCustomerId();

    String getCustomerName();

    Integer getDriverId();

    String getOrderStatus();

    BigDecimal getMerchantTotalPrice();
}