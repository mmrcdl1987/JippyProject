package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CoGroupCartItemsDto {

    @NotNull(message = "Group Orders Invitation ID cannot be null")
    private Integer groupOrderInvitationId;

    @NotNull(message = "Customer ID cannot be null")
    private Integer customerId;

    @NotNull(message = "Product ID cannot be null")
    private Integer productId;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

   // private BigDecimal merchantUnitPrice;

    @NotNull(message = "Online Unit Price cannot be null")
    private BigDecimal onlineUnitPrice;

    @NotNull(message = "Created By cannot be null")
    private Integer createdBy;

}
