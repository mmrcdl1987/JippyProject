package com.jippy.division.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DivOrderDto {

    private String orderId;

    private Integer customerId;

    private String orderStatus;

    private Integer paymentModeId;

    private Integer outletId;

    private BigDecimal orderTotalAmount;
}
