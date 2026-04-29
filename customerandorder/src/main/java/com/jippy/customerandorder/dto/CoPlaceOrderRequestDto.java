package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoPlaceOrderRequestDto {

    @NotNull
    private Integer outletId;

    @NotNull
    private Integer customerId;

    @NotNull
    private Integer customerDeliveryAddressId;

    @NotNull
    private String customerPhone;

    private Integer couponId;

    @NotNull
    private BigDecimal orderAmount;

    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal surgeFee;
    private BigDecimal packagingFee;
    private BigDecimal gst;

    @NotNull
    private BigDecimal orderTotalAmount;

    private BigDecimal couponDiscount;

    @NotEmpty
    private List<CoOrderItemDto> items;
}