package com.jippy.customerandorder.projection;

/**
 * Projection used to fetch complete order flow counts
 * directly from the orders table.
 */
public interface CoCompleteOrdersFlowCountsProjection {

    Long getTotalOrdersCount();

    Long getOrdersPlaced();

    Long getOrdersConfirmed();

    Long getOrdersShipped();

    Long getOrdersCompleted();

    Long getOrdersRejected();

}