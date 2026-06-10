package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;

import java.util.List;


public interface CoCustomerDeliveryService {

    CoCustomerUnreachableResponseDto customerUnreachable(CoCustomerUnreachableRequestDto requestDto);

    CoFinalRejectResponseDto finalRejectOrder(CoFinalRejectRequestDto requestDto);

    //    gets the customer location based on the customer address id
    CoCustomerDeliveryAddressResponseDto createCustomerDeliveryAddress
    (CoCustomerDeliveryAddressRequestDto requestDto);

//    to get list of all the delivery addresses of a customer based on the customer id
    List<CoCustomerDeliveryAddressResponseDto> getCustomerDeliveryAddresses(Integer customerId);

    //     to delete a delivery address based on the customer_address_id
    void deleteCustomerDeliveryAddress(Integer customerAddressId);

}