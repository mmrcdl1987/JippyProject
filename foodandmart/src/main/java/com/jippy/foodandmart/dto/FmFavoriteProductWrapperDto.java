package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmFavoriteProductWrapperDto{

    private List<FmFavoriteProductResponseDto> favoriteProducts;

}

