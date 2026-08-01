package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoCurrentMealTypeResponse {

    /**
     * Meal Type Timing ID
     */
    private Integer mealTypeTimingId;

    /**
     * Current Active Meal Type
     */
    private String mealType;

}