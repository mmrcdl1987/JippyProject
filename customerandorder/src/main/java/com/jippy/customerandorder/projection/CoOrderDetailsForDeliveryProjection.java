package com.jippy.customerandorder.projection;

public interface CoOrderDetailsForDeliveryProjection {

    String getOrderId();

    String getFirstName();

    String getPhoneNumber();

    Double getCustomerLatitude();

    Double getCustomerLongitude();

    String getCustomerAddress();

    Integer getProductId();

    Double getOrderTotalAmount();

    Integer getQuantity();

    Integer getVariantOptionId();

    Integer getOutletId();

}
