package com.jippy.customerandorder.iservice;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;

public interface ICartService {

    String updateCart(CoCartUpdateRequestDto dto);

}
