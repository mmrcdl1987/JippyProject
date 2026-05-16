package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;

public interface IOrderSettingsService {
    CoOrderSettingsResponseDto saveOrUpdate(CoOrderSettingsRequestDto requestDto);

}
