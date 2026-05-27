package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;

public interface CoWalletSettingsService {

    CoWalletSettingsResponseDto saveWalletSettings(CoWalletSettingsRequestDto requestDto);
}