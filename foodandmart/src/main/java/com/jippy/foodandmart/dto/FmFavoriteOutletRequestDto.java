package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FmFavoriteOutletRequestDto {

    private Integer customerId;
    private Integer outletId;
    private Integer createdBy;
}