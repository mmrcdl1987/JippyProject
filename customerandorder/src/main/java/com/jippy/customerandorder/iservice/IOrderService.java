package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;

import java.util.List;

public interface IOrderService {

     CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto);

     List<Integer> getFrequentOutlets(Integer customerId);

     Integer getRecentOutlet(Integer customerId);

    CoOrderDto getOrder(String orderId);

    void updateOrderStatus(CoOrderDto orderDto);

    CoOrderPriceBreakupDto getOrderPriceBreakup(String orderId);

    String acceptOrRejectOrderByOutlet(AcceptOrRejectOrderByOutletDto acceptOrRejectOrderByOutletDto);
}
