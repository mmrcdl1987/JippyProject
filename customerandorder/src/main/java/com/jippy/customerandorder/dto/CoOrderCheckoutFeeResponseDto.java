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
public class CoOrderCheckoutFeeResponseDto {

    private Integer orderCheckoutFeeId;

    private BigDecimal platformFee;

    private Boolean platformFeeToggle;

    private BigDecimal surgeFee;

    private Boolean surgeFeeToggle;

    private BigDecimal packagingFee;

    private Boolean packagingFeeToggle;

    private Integer areaId;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;
}