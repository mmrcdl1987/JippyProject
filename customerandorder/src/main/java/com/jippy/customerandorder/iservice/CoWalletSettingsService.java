package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CoWalletSettingsService {

    /**
     * Create / Update Wallet Settings.
     */
    CoWalletSettingsResponseDto saveWalletSettings(
            CoWalletSettingsRequestDto requestDto);

    /**
     * Get all Wallet Settings without pagination.
     */
    List<CoWalletSettings> getWalletSettings();

    /**
     * Get Wallet Settings with Pagination.
     *
     * @param page page number starting from 0
     * @param size number of records per page
     */
    Page<CoWalletSettingsResponseDto> getWalletSettings(
            int page,
            int size);

    List<CoWalletSettings> getWalletSettings();
}