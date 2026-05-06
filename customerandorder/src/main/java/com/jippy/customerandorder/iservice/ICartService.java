package com.jippy.customerandorder.iservice;
import com.jippy.customerandorder.dto.CoCartResponseDto;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;

public interface ICartService {

    String updateCart(CoCartUpdateRequestDto dto);

    CoCartResponseDto getCart(Integer customerId);
}
