package com.jippy.customerandorder.repository;

import java.math.BigDecimal;

public interface CoDriverEarningsProjection {

    BigDecimal getTotalEarnings();

    Long getOrdersCount();
}