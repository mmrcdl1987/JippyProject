package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;

import java.time.LocalDateTime;

public final class CoWalletSettingsMapper {

    private CoWalletSettingsMapper() {
        // Utility class
    }

    /**
     * Maps Wallet Settings Request DTO to Entity.
     *
     * @param requestDto request DTO
     * @return mapped entity
     */
    public static CoWalletSettings mapToEntity(
            CoWalletSettingsRequestDto requestDto) {

        if (requestDto == null) {
            return null;
        }

        CoWalletSettings entity = new CoWalletSettings();

        // ========================================================
        // SETTING TYPE
        // ========================================================

        entity.setSettingType(
                requestDto.getSettingType() != null
                        ? requestDto.getSettingType().trim()
                        : null
        );

        // ========================================================
        // SETTING VALUE
        // ========================================================
        // settingValue is Integer.
        // Do NOT use trim().
        // ========================================================

        entity.setSettingValue(
                requestDto.getSettingValue()
        );

        // ========================================================
        // AUDIT FIELDS
        // ========================================================

        entity.setCreatedBy(
                requestDto.getCreatedBy()
        );

        entity.setCreatedAt(
                LocalDateTime.now()
        );

        return entity;
    }

    /**
     * Maps Wallet Settings Entity to Response DTO.
     *
     * @param entity wallet settings entity
     * @return mapped response DTO
     */
    public static CoWalletSettingsResponseDto mapToResponseDto(
            CoWalletSettings entity) {

        if (entity == null) {
            return null;
        }

        CoWalletSettingsResponseDto responseDto =
                new CoWalletSettingsResponseDto();

        // ========================================================
        // ID
        // ========================================================

        responseDto.setWalletSettingsId(
                entity.getWalletSettingsId()
        );

        // ========================================================
        // SETTING TYPE
        // ========================================================

        responseDto.setSettingType(
                entity.getSettingType()
        );

        // ========================================================
        // SETTING VALUE
        // ========================================================

        responseDto.setSettingValue(
                entity.getSettingValue()
        );

        // ========================================================
        // CREATE AUDIT FIELDS
        // ========================================================

        responseDto.setCreatedAt(
                entity.getCreatedAt()
        );

        responseDto.setCreatedBy(
                entity.getCreatedBy()
        );

        // ========================================================
        // UPDATE AUDIT FIELDS
        // ========================================================

        responseDto.setUpdatedAt(
                entity.getUpdatedAt()
        );

        responseDto.setUpdatedBy(
                entity.getUpdatedBy()
        );

        return responseDto;
    }
}