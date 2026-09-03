package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmProfilePicUpdateDto {

    private Integer merchantId;

    private String profilePicUrl;
}