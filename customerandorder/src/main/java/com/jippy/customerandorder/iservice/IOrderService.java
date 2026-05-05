package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.dto.CoPlaceOrderResponseDto;

public interface IOrderService {

     CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto);

}
