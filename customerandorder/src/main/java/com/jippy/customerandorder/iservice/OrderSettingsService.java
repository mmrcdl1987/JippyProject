package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface OrderSettingsService {

    CoOrderSettingsResponseDto saveOrUpdate(@Valid CoOrderSettingsRequestDto requestDto);

    CoPaymentModeResponse getPaymentModeById(Integer paymentModeId);

    List<CoPaymentModeResponse> getActivePaymentModes();
}
