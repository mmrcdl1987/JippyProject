package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FmDivPriceModelDto {
    private Integer priceModelId;
    private String priceModelName;
    private Integer createdBy;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdAt;

}
