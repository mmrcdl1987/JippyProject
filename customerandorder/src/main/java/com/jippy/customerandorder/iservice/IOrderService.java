package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderDto;
import com.jippy.customerandorder.dto.CoOrderPriceBreakupDto;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.dto.CoPlaceOrderResponseDto;

import java.util.List;

public interface IOrderService {

     CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto);

     List<Integer> getFrequentOutlets(Integer customerId);

     Integer getRecentOutlet(Integer customerId);

    CoOrderDto getOrder(String orderId);

    void updateOrderStatus(CoOrderDto orderDto);

    CoOrderPriceBreakupDto getOrderPriceBreakup(String orderId);
}
