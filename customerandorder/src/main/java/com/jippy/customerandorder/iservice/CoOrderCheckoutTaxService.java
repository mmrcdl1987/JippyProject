package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderCheckoutTaxRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutTaxResponseDto;

import java.util.List;

public interface CoOrderCheckoutTaxService {

    CoOrderCheckoutTaxResponseDto create(
            CoOrderCheckoutTaxRequestDto request
    );

    CoOrderCheckoutTaxResponseDto getById(
            Integer orderCheckoutTaxId
    );

    List<CoOrderCheckoutTaxResponseDto> getAll();

    CoOrderCheckoutTaxResponseDto update(
            Integer orderCheckoutTaxId,
            CoOrderCheckoutTaxRequestDto request
    );

    void delete(Integer orderCheckoutTaxId);
}