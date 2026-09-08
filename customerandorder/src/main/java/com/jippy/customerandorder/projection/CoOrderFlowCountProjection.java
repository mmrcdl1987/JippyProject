package com.jippy.customerandorder.projection;

/**
 * Projection used to fetch order flow counts
 * directly from the orders table.
 */
public interface CoOrderFlowCountProjection {

    Long getTotalOrdersCount();

    Long getCompletedOrdersCount();

    Long getRejectedOrdersCount();
}