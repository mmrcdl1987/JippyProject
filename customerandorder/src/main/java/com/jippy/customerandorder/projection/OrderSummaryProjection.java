package com.jippy.customerandorder.projection;

import com.jippy.customerandorder.dto.CoOrderSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderSummaryProjection {

    Integer getOrderItemId();

    String getOrderId();

    Integer getProductId();

    String getOrderStatus();

    BigDecimal getMerchantUnitPrice();

    BigDecimal getMerchantTotalPrice();

    Integer getVariantOptionId();

    Integer getQuantity();

    String getCookingInstructions();

    Boolean getIsCutleryRequired();

    LocalDateTime getCreatedAt();


}
