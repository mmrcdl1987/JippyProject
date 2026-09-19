package com.jippy.foodandmart.projections;

public interface OrderSummaryProjection {

    String getOrderId();

    Integer getProductId();
    Integer getVariantOptionId();
    Integer getQuantity();
    String getOrderStatus();

}
