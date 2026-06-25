package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletWrapperDto;

import java.util.List;

public interface FmFavoriteOutletService {

    FmFavoriteOutletResponseDto toggleFavorite(FmFavoriteOutletRequestDto dto);

    //changed for production
//    void removeFavorite(Integer customerId, Integer outletId);

//    List<FmFavoriteOutletResponseDto> getFavorites(Integer customerId);

    FmFavoriteOutletWrapperDto getFavorites(Integer customerId);
}
