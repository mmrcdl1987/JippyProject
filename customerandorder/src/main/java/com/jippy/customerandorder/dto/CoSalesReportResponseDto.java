package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoSalesReportResponseDto {

    private Long totalOrders;

    private BigDecimal totalEarnings;

    private List<CoDailySalesReportDto> dailyBreakdown;
}