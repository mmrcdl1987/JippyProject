package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoReorderRequestDto;
import com.jippy.customerandorder.dto.CoReorderResponseDto;

public interface ICoReorderService {

    CoReorderResponseDto reorder(CoReorderRequestDto requestDto);
}