package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FmCuisineTypeResponseDTO {

    private Integer cuisineTypesId;

    private String cuisineTypesName;

}