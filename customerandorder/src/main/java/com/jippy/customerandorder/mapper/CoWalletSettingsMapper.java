package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;

import java.time.LocalDateTime;

public class CoWalletSettingsMapper {

    private CoWalletSettingsMapper() {
    }

    // Request DTO -> Entity
    public static CoWalletSettings mapToEntity(
            CoWalletSettingsRequestDto requestDto) {

        CoWalletSettings entity = new CoWalletSettings();


        entity.setSettingType(requestDto.getPointsType());
        entity.setSettingValue(requestDto.getNumOfPoints());
        entity.setCreatedBy(requestDto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    // Entity -> Response DTO
    public static CoWalletSettingsResponseDto mapToResponseDto(
            CoWalletSettings entity) {

        CoWalletSettingsResponseDto responseDto =
                new CoWalletSettingsResponseDto();

        responseDto.setWalletSettingsId(entity.getWalletSettingsId());
        responseDto.setSettingType(entity.getSettingType());
        responseDto.setSettingValue(entity.getSettingValue());
        responseDto.setCreatedAt(entity.getCreatedAt());
        responseDto.setCreatedBy(entity.getCreatedBy());
        responseDto.setUpdatedAt(entity.getUpdatedAt());
        responseDto.setUpdatedBy(entity.getUpdatedBy());

        return responseDto;
    }
}