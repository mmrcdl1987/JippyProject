package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupOrderDeliveryChargeDetailsDto {

    @NotNull(message = "Delivery charge is required")
    private BigDecimal deliveryCharge;

    @NotNull(message = "Tax amount is required")
    private BigDecimal taxAmount;

    @NotNull(message = "Total delivery charge is required")
    private BigDecimal totalDeliveryCharge;

    @NotNull(message = "Delivery address ID is required")
    private Integer deliveryAddressId;

    @NotNull(message = "Food tax is required")
    private BigDecimal foodTax;

    @NotNull(message = "Customer payment details list cannot be null")
    private List<GroupOrderCustomerPaymentDetailsDto> customerPaymentDetailsDtoList;
}
