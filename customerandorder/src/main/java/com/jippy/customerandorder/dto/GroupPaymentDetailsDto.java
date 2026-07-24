package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupPaymentDetailsDto {

    @NotNull(message = "Group Order Invitation Id is not null")
    private Integer groupOrderInvitationId;

    @NotNull(message = "Platform Fee is not null")
    private BigDecimal platformFee;

    private BigDecimal surgeFee;

    @NotNull(message = "Packaging fee is not null")
    private BigDecimal packagingFee;

    @NotNull(message = "Host Customer Id is not null")
    private Integer hostCustomerId;

    @NotNull(message = "Total net amount is not null")
    private BigDecimal totalNetAmount;

    @NotNull(message = "Group Order Delivery Charge Details List is not null")
    private List<GroupOrderDeliveryChargeDetailsDto> groupOrderDeliveryChargeDetailsDtoList;
}
