package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface GroupOrderCartItemsProjection {

    Integer getGroupCartItemId();
    Integer getGroupOrdersInvitationId();
    Integer getCustomerId();
    Integer getProductId();
    Integer getQuantity();
    BigDecimal getOnlineUnitPrice();
    Integer getDeliveryAddressId();
    Integer getMerchantUnitPrice();

}
