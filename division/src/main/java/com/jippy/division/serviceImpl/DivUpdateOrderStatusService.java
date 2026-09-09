package com.jippy.division.serviceImpl;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.dto.PaymentVerifyRequestDto;
import com.jippy.division.feignClient.CoFeignClient;
import com.sun.jdi.PrimitiveValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DivUpdateOrderStatusService {

    private final CoFeignClient coFeignClient;

    void updateOrderStatus(String orderId, boolean isSuccess){

        DivOrderDto orderDto = new DivOrderDto();

        if(isSuccess){
            orderDto.setOrderStatus(DivAppConstants.ORDER_PLACED);
        }else{
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_FAILED);
        }

        orderDto.setOrderId(orderId);

        coFeignClient.updateOrderStatus(orderDto);
    }
}
