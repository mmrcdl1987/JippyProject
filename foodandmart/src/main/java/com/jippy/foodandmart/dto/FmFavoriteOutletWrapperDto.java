package com.jippy.foodandmart.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FmFavoriteOutletWrapperDto {

//    wrapping both the favorite outlets and the frequent outlets in a single response
// Customer's favourite outlets
    private List<FmFavoriteOutletResponseDto> favorites;
    //    private List<Integer> frequentOutlets;

    // Frequently ordered outlets with complete outlet details
    private List<FmFavoriteOutletResponseDto> frequentOutlets;

    //    private Integer recentOutlet;

    // Most recently ordered outlet with complete outlet details
    private FmFavoriteOutletResponseDto recentOutlet;
}
