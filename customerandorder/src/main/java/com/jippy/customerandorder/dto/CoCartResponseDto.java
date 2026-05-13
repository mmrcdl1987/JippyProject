package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoCartResponseDto {
    private Integer customerId;
    private List<CoCartItemResponseDto> items;
    private BigDecimal grandTotal;
}