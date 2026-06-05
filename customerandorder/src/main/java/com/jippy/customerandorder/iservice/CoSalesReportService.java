package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoSalesReportResponseDto;
import com.jippy.customerandorder.enums.CoSalesReportFilter;

public interface CoSalesReportService {

    CoSalesReportResponseDto getSalesReport(Integer merchantId, Integer outletId, CoSalesReportFilter filter);
}