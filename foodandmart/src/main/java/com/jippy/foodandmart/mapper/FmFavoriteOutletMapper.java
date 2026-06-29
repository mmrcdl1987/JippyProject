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
        entity.setOutletId(dto.getOutletId());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
//from the entity to the response dto
//    to get the response dto from the entity
    public static FmFavoriteOutletResponseDto toFavOutletDto(FmFavoriteOutlet entity) {
        FmFavoriteOutletResponseDto dto = new FmFavoriteOutletResponseDto();
        dto.setFavoriteOutletId(entity.getFavoriteOutletsId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setOutletId(entity.getOutletId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setIsFavourite(true);

        return dto;
    }

    public static FmFavoriteOutletResponseDto toFavoriteOutletResponseDto(
            Integer customerId,
            Integer outletId,
            FmOutlet outlet,
            FmFavoriteOutlet favourite) {

        FmFavoriteOutletResponseDto dto = new FmFavoriteOutletResponseDto();

        dto.setCustomerId(customerId);
        dto.setOutletId(outletId);

        if (outlet != null) {
            dto.setOutletName(outlet.getOutletName());
            dto.setOutletPicUrl(outlet.getOutletPicUrl());
            dto.setReview(outlet.getReview());
        }

        if (favourite != null) {
            dto.setFavoriteOutletId(favourite.getFavoriteOutletsId());
            dto.setCreatedAt(favourite.getCreatedAt());
            dto.setIsFavourite(true);
        } else {
            dto.setIsFavourite(false);
        }

        return dto;
    }
}