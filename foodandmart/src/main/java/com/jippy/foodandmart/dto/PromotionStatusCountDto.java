package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionStatusCountDto {

    private long active;

    private long scheduled;

    private long ended;

    private long total;

}