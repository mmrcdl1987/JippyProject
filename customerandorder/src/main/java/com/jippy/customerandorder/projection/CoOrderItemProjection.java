package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOrderItemProjection {

    Integer getProductId();

    Integer getVariantOptionId();

    Integer getQuantity();

    BigDecimal getOnlineUnitPrice();

    BigDecimal getOnlinePriceTotal();
}