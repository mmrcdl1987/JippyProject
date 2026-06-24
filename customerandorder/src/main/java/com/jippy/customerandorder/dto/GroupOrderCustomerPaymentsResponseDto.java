package com.jippy.customerandorder.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupOrderCustomerPaymentsResponseDto {

    private Integer groupOrderPaymentsId;
    private Integer customerId;
    private String customerName;
    private BigDecimal amountToPay;
    private String paymentStatus;

}
