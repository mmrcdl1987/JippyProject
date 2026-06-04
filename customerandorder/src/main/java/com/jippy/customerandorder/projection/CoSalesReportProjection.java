package com.jippy.customerandorder.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CoSalesReportProjection {

    LocalDate getSalesDate();

    Long getTotalOrders();

    BigDecimal getTotalEarnings();
}