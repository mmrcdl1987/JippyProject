package com.jippy.foodandmart.mapper;


import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmCityDto;
import com.jippy.foodandmart.dto.FmStateDto;
import com.jippy.foodandmart.entity.FmArea;
import com.jippy.foodandmart.entity.FmCity;
import com.jippy.foodandmart.entity.FmState;

public class FmLocationMapper {

    // For State (get) --mapToStateDto
    public static FmStateDto mapToStateDto(FmState stateEntity) {

        FmStateDto stateDto = new FmStateDto();

        stateDto.setStateId(stateEntity.getStateId());
        stateDto.setStateName(stateEntity.getStateName());

        return stateDto;
    }
// to post State -- mapToStateEntity
    public static FmState mapToStateEntity(FmStateDto dto) {

        FmState entity = new FmState();

        entity.setStateId(dto.getStateId());
        entity.setStateName(dto.getStateName());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setUpdatedBy(dto.getUpdatedBy());

        return entity;
    }

    // for City (get) -- mapToCityDto
    public static FmCityDto mapToCityDto(FmCity cityEntity) {

        FmCityDto cityDto = new FmCityDto();

        cityDto.setCityId(cityEntity.getCityId());
        cityDto.setCityName(cityEntity.getCityName());

        return cityDto;
    }

// to post city --mapToCityEntity
    public static FmCity mapToCityEntity(FmCityDto dto) {

        FmCity entity = new FmCity();

        entity.setCityId(dto.getCityId());
        entity.setCityName(dto.getCityName());
        entity.setStateId(dto.getStateId());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setUpdatedBy(dto.getUpdatedBy());

        return entity;
    }

    // For Area (get) --mapToAreaDto
    public static FmAreaDto mapToAreaDto(FmArea areaEntiity) {

        FmAreaDto areaDto = new FmAreaDto();

        areaDto.setAreaId(areaEntiity.getAreaId());
        areaDto.setAreaName(areaEntiity.getAreaName());
        return areaDto;
    }
// to post Area --mapToAreaEntity
    public static FmArea mapToAreaEntity(FmAreaDto dto) {

        FmArea entity = new FmArea();

        entity.setAreaId(dto.getAreaId());
        entity.setAreaName(dto.getAreaName());
        entity.setCityId(dto.getCityId());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setUpdatedBy(dto.getUpdatedBy());

        return entity;
    }
}