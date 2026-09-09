package com.jippy.division.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DivRefundDetailsProjection {

    String getApplicationOrderId();

    String getPaymentTransactionsId();

    BigDecimal getAmountInPaise();

    String getRefundStatus();

    String getReason();

    LocalDateTime getCreatedAt();
}