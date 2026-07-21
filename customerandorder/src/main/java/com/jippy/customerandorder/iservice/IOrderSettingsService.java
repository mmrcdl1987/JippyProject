package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModesDto;
import com.jippy.customerandorder.entity.CoPaymentModes;

public interface IOrderSettingsService {
    CoOrderSettingsResponseDto saveOrUpdate(CoOrderSettingsRequestDto requestDto);

    CoPaymentModesDto getPaymentModeById(Integer paymentModeId);
}
