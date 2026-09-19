package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FmMerchantOrderSummaryDto {

    private String orderStatus;
    private String orderId;
    private BigDecimal totalPrice;
    private String cookingInstructions;
    private Boolean isCutleryRequired;
    private LocalDateTime orderCreatedAt;
    private List<FmMerchantOrderProductDto> merchantOrderProductDtoList;

}
