package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;

import java.util.List;

public interface CoWalletSettingsService {

    List<CoWalletSettings> getWalletSettings();

    CoWalletSettingsResponseDto saveWalletSettings(CoWalletSettingsRequestDto requestDto);
}