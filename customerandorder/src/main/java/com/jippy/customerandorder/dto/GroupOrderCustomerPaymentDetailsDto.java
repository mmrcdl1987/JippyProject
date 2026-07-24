package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupOrderCustomerPaymentDetailsDto {

    @NotNull(message = "Customer Id is not null")
    private Integer customerId;

    @NotNull(message = "Customer name is not null")
    private String customerName;

    @NotNull(message = "Amount to pay is not null")
    private BigDecimal amountToPay;


}
