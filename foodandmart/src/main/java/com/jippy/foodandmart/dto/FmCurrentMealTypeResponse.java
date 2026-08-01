package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmCurrentMealTypeResponse {

    private Integer mealTypeTimingId;

    private String mealType;

}