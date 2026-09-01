package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderCheckoutFeeRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutFeeResponseDto;

import java.util.List;

public interface CoOrderCheckoutFeeService {

    CoOrderCheckoutFeeResponseDto create(
            CoOrderCheckoutFeeRequestDto request
    );

    CoOrderCheckoutFeeResponseDto getById(
            Integer orderCheckoutFeeId
    );

    List<CoOrderCheckoutFeeResponseDto> getAll();

    CoOrderCheckoutFeeResponseDto update(
            Integer orderCheckoutFeeId,
            CoOrderCheckoutFeeRequestDto request
    );

    void delete(Integer orderCheckoutFeeId);
}