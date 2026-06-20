package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupOrderCustomerPaymentDetailsDto {

    private Integer customerId;
    private String customerName;
    private BigDecimal amountToPay;


}
