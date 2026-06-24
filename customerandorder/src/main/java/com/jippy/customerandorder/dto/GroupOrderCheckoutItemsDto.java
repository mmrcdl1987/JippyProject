package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupOrderCheckoutItemsDto {


    private Integer customerId;
   private String customerName;
    private BigDecimal amountToPay;
    private List<ProductItemDto> productsList; //
}
