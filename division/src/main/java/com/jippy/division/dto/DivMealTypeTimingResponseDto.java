package com.jippy.division.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DivMealTypeTimingResponseDto {

    private Integer mealTypeTimingsId;

    private String mealType;

    private String fromTime;

    private String toTime;
}
