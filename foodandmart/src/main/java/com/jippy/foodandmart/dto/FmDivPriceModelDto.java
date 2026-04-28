package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FmDivPriceModelDto {
    private Integer priceModelId;
    private String priceModelName;
    private Integer createdBy;
    private LocalDateTime createdAt;

}
