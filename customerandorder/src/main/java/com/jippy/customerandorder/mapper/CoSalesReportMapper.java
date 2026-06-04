package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoDailySalesReportDto;
import com.jippy.customerandorder.projection.CoSalesReportProjection;
import org.springframework.stereotype.Component;

@Component
public class CoSalesReportMapper {

    public CoDailySalesReportDto mapToDailySalesDto(CoSalesReportProjection projection) {

        CoDailySalesReportDto dto = new CoDailySalesReportDto();

        dto.setSalesDate(projection.getSalesDate());

        dto.setDayName(projection.getSalesDate().getDayOfWeek().name());

        dto.setTotalOrders(projection.getTotalOrders());

        dto.setTotalEarnings(projection.getTotalEarnings());

        return dto;
    }
}