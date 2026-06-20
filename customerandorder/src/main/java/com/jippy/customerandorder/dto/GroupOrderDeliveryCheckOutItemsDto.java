package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class GroupOrderDeliveryCheckOutItemsDto {

    private BigDecimal deliveryDistanceKm;

    private BigDecimal deliveryCharge;

    private BigDecimal taxAmount;

    private BigDecimal totalDeliveryCharge;

    private BigDecimal itemsTotal;

    private BigDecimal foodTax;

    private Integer deliveryAddressId;

    private List<GroupOrderCheckoutItemsDto> groupOrderCheckoutItemsDtoList;
}
