package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class CoNewOrderEvent {

    private String orderId;
    private Integer customerId;
    private Integer outletId;
    private String customerName;
    private String customerMobileNum;
    private String orderStatus;
    private List<CoOrderItemsEvent> orderItemsList;
    private String cookingInstructions;
    private Boolean cutleryRequired;

}
