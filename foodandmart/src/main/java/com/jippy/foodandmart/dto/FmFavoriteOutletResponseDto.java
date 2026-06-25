package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FmFavoriteOutletResponseDto {

    private Integer favoriteOutletId;
    private Integer customerId;
    private Integer outletId;
    private LocalDateTime createdAt;

    //changed for production
    private Boolean isFavourite;
  //  from Outlet Table
    private BigDecimal review;
    private String outletName;
    private String outletPicUrl;
}