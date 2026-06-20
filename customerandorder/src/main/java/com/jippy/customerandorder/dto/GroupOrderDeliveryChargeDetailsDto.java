package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupOrderDeliveryChargeDetailsDto {

    private BigDecimal deliveryCharge;
    private BigDecimal taxAmount;
    private BigDecimal totalDeliveryCharge;
    private Integer deliveryAddressId;
    private BigDecimal foodTax;
    private List<GroupOrderCustomerPaymentDetailsDto> customerPaymentDetailsDtoList;
}
