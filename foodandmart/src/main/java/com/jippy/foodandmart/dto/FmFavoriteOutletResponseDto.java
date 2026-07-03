package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FmFavoriteOutletResponseDto {

    // Favourite Details
    private Integer favoriteOutletId;
    private Integer customerId;
//   private Integer outletId;

   //outletID = favoriteId
    private Integer favoriteId;
    private String favouriteType;
    private Boolean isFavourite;

    // Outlet Details
    private BigDecimal review;
    private String outletName;
    private String outletPicUrl;

}