package com.jippy.customerandorder.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Projection interface for native query result from Repository
public interface CoOrderSettlementProjection {

    String getOrderId();

    Integer getOutletId();

    String getOrderStatus();

    LocalDateTime getCreatedAt();

    BigDecimal getTotalPrice();
}