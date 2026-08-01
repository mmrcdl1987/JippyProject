package com.jippy.customerandorder.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CoCartReminderProjection {

    Integer getCustomerId();

    BigDecimal getCartTotal();

    LocalDateTime getLastUpdated();

}