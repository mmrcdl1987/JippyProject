package com.jippy.foodandmart.dto;

public record FmOrderProductDtoWrapper(  FmOrderSummaryDto orderDto,
                                         FmMerchantOrderProductDto productDto) {
}
