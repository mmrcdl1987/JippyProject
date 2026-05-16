package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoDriverEarningsProjection {

    BigDecimal getTotalEarnings();

    Long getOrdersCount();
}