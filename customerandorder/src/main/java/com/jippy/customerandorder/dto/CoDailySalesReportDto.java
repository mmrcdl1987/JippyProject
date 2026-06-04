package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoDailySalesReportDto {

    private LocalDate salesDate;

    private String dayName;

    private Long totalOrders;

    private BigDecimal totalEarnings;
}