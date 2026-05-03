package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCartUpdateRequestDto  {

    @NotNull
    private Integer customerId;

    @NotNull
    private Integer productId;

    @NotNull
    private Integer quantity;   // UI sends updated qty

    @NotNull
    private BigDecimal unitPrice; // UI sends price
}