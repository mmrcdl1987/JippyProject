package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.dto.FmProductPriceSettingsResponseDto;
import org.springframework.data.domain.Page;

public interface IFmProductPriceSettingsService {

    FmProductPriceSettingsResponseDto create(FmProductPriceSettingsRequestDto request, Integer userId);

    FmProductPriceSettingsResponseDto getById(Integer id);

    Page<FmProductPriceSettingsResponseDto> getAll(int page, int size);

    FmProductPriceSettingsResponseDto update(Integer id, FmProductPriceSettingsRequestDto request, Integer userId);

    void delete(Integer id);
}