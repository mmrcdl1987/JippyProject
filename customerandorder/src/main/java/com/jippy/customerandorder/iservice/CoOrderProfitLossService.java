package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderProfitLossDto;
import com.jippy.customerandorder.enums.DateRangeFilter;

import java.time.LocalDate;
import java.util.List;

public interface CoOrderProfitLossService {

    CoOrderProfitLossDto getOrderProfitLoss(String orderId);

    List<CoOrderProfitLossDto> getAllOrdersProfitLoss(
            LocalDate fromDate,
            LocalDate toDate,
            DateRangeFilter dateRangeFilter
    );
}
