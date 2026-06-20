package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
public class GroupOrderCheckoutDto {

    private Integer groupOrdersInvitationId;
    private BigDecimal platformFee;
    private BigDecimal surgeFee;
    private BigDecimal packagingFee;
    private BigDecimal totalNetAmount;
    private List<GroupOrderDeliveryCheckOutItemsDto> deliveryCheckOutItemsDtoList;


}
