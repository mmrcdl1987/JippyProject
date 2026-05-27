package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoPlaceOrderRequestDto {

    @NotNull(message = "Outlet id is required")
    private Integer outletId;

    @NotNull(message = "Customer id is required")
    private Integer customerId;

    @NotNull(message = "Customer delivery address id is required")
    private Integer customerDeliveryAddressId;

    @NotNull(message = "Customer phone is required")
    private String customerPhone;

    @NotNull(message = "Payment mode id is required")
    private Integer paymentModeId;

    private Integer couponId;

    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;

    private BigDecimal platformFee;

    private BigDecimal deliveryFee;

    private BigDecimal surgeFee;

    private BigDecimal packagingFee;

    private BigDecimal gst;

    @NotNull(message = "Order total amount is required")
    private BigDecimal orderTotalAmount;

    private BigDecimal couponDiscount;

    @NotEmpty(message = "Order items cannot be empty")
    private List<CoOrderItemDto> items;
}