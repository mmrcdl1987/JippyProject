package com.jippy.customerandorder.projection;

public interface CustomerDeliveryAddressProjection {

    Double getLatitude();

    Double getLongitude();

    Integer getCustomerAddressId();

    Integer getCustomerId();

    String getBuildingName();

    String getLaneNo();

    String getDoorNo();

    Integer getArea();
}
