package com.jippy.foodandmart.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FmFavoriteOutletWrapperDto {

//    wrapping both the favorite outlets and the frequent outlets in a single response
    private List<FmFavoriteOutletResponseDto> favorites;
    private List<Integer> frequentOutlets;
    private Integer recentOutlet;
}