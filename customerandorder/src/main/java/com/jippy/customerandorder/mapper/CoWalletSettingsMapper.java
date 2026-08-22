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
     * Request DTO -> Entity
     */
    public static CoWalletSettings mapToEntity(
            CoWalletSettingsRequestDto requestDto) {

        if (requestDto == null) {
            return null;
        }

        CoWalletSettings entity = new CoWalletSettings();

        entity.setPointsType(
                requestDto.getPointsType() != null
                        ? requestDto.getPointsType().trim()
                        : null
        );

        entity.setNumOfPoints(requestDto.getNumOfPoints());
        entity.setCreatedBy(requestDto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    /**
     * Entity -> Response DTO
     */
    public static CoWalletSettingsResponseDto mapToResponseDto(
            CoWalletSettings entity) {

        if (entity == null) {
            return null;
        }

        CoWalletSettingsResponseDto responseDto =
                new CoWalletSettingsResponseDto();

        responseDto.setWalletSettingsId(entity.getWalletSettingsId());
        responseDto.setPointsType(entity.getPointsType());
        responseDto.setNumOfPoints(entity.getNumOfPoints());
        responseDto.setCreatedAt(entity.getCreatedAt());
        responseDto.setCreatedBy(entity.getCreatedBy());
        responseDto.setUpdatedAt(entity.getUpdatedAt());
        responseDto.setUpdatedBy(entity.getUpdatedBy());

        return responseDto;
    }
}