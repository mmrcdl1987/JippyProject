package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupOrderPaymentDetailsResponseDto {
    private BigDecimal totalNetAmount;
    private List<GroupOrderCustomerPaymentsResponseDto> groupOrderCustomerPaymentsResponeDtoList;

}
