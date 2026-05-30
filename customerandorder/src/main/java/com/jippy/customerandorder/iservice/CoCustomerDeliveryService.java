package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoCustomerUnreachableRequestDto;
import com.jippy.customerandorder.dto.CoCustomerUnreachableResponseDto;
import com.jippy.customerandorder.dto.CoFinalRejectRequestDto;
import com.jippy.customerandorder.dto.CoFinalRejectResponseDto;


public interface CoCustomerDeliveryService {

    CoCustomerUnreachableResponseDto customerUnreachable(
            CoCustomerUnreachableRequestDto requestDto);

    CoFinalRejectResponseDto finalRejectOrder(
            CoFinalRejectRequestDto requestDto);
}