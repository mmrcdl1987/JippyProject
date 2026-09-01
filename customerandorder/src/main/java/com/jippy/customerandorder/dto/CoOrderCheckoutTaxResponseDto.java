package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoOrderCheckoutTaxResponseDto {

    private Integer orderCheckoutTaxId;

    private BigDecimal platformFeeTax;

    private BigDecimal surgeFeeTax;

    private BigDecimal packagingFeeTax;

    private BigDecimal deliveryFeeTax;

    private BigDecimal foodAmountTax;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;
}