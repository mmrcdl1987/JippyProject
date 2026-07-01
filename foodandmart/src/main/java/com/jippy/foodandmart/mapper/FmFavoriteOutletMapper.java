package com.jippy.foodandmart.mapper;



import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import com.jippy.foodandmart.entity.FmOutlet;

import java.time.LocalDateTime;

public class FmFavoriteOutletMapper {

//    from the request dto to the entity
//    to post the request dto to the entity
    public static FmFavoriteOutlet toFavOutletEntity(FmFavoriteOutletRequestDto dto) {
        FmFavoriteOutlet entity = new FmFavoriteOutlet();
        entity.setCustomerId(dto.getCustomerId());
//        entity.setOutletId(dto.getOutletId());
        entity.setFavoriteId(dto.getFavoriteId());
        entity.setFavouriteType(dto.getFavouriteType().toUpperCase());
        entity.setCreatedBy(dto.getCustomerId());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
//from the entity to the response dto
//    to get the response dto from the entity
    public static FmFavoriteOutletResponseDto toFavOutletDto(FmFavoriteOutlet entity) {
        FmFavoriteOutletResponseDto dto = new FmFavoriteOutletResponseDto();
        dto.setFavoriteOutletId(entity.getFavoriteOutletsId());
        dto.setCustomerId(entity.getCustomerId());
//        dto.setOutletId(entity.getOutletId());
        dto.setFavoriteId(entity.getFavoriteId());
        dto.setFavouriteType(entity.getFavouriteType().toUpperCase());
//        dto.setCreatedAt(entity.getCreatedAt());
        dto.setIsFavourite(true);

        return dto;
    }

}