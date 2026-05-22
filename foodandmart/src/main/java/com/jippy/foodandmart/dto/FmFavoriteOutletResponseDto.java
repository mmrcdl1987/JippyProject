package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FmFavoriteOutletResponseDto {

    private Integer favoriteOutletId;
    private Integer customerId;
    private Integer outletId;
    private LocalDateTime createdAt;
}