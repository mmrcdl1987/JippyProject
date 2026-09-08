package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

/**
 * Projection used to fetch order details from CO database.
 *
 * FM outlet details and Driver details are not fetched here
 * because they belong to separate microservices/databases.
 */
public interface CoOrderDetailsByOrderStatusProjection {

    String getOrderId();

    Integer getOutletId();

    Integer getDriverId();

    String getOrderStatus();

    String getCustomerName();

    BigDecimal getOrderAmount();
}