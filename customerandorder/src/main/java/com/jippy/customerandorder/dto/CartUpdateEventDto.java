package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartUpdateEventDto {

    private String action; // e.g., "ITEM_ADDED", "ITEM_REMOVED", "QUANTITY_CHANGED"
    private Integer invitationId;
    private Integer customerId;
    private String customerName;
    private String itemName; // e.g., "Pepperoni Pizza"
    private Integer quantity;
    private BigDecimal itemPrice;

}
