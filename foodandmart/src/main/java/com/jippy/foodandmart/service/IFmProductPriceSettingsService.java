package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.dto.FmProductPriceSettingsResponseDto;
import org.springframework.data.domain.Page;

public interface IFmProductPriceSettingsService {

    FmProductPriceSettingsResponseDto create(FmProductPriceSettingsRequestDto request, Integer userId);

    FmProductPriceSettingsResponseDto getById(Integer id);

    Page<FmProductPriceSettingsResponseDto> getAll(int page, int size);

    FmProductPriceSettingsResponseDto update(Integer id, FmProductPriceSettingsRequestDto request, Integer userId);

//    //void delete(Integer id);
//    /*
//     * Soft delete.
//     *
//     * This will NOT delete the database row.
//     * It will only set:
//     *
//     * is_active = 'N'
//     */
//    void delete(Integer id);
//
//    /*
//     * Restore / Activate.
//     *
//     * This will set:
//     *
//     * is_active = 'Y'
//     */
//    void restore(Integer id);
    void updateStatus(Integer id, String status);
}