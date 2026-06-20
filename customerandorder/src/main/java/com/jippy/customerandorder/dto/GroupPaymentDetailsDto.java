package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupPaymentDetailsDto {

    private Integer groupOrderInvitationId;
    private BigDecimal platformFee;
    private BigDecimal surgeFee;
    private BigDecimal packagingFee;
    private Integer hostCustomerId;
    private BigDecimal totalNetAmount;
    private List<GroupOrderDeliveryChargeDetailsDto> groupOrderDeliveryChargeDetailsDtoList;
}
