package com.jippy.foodandmart.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@NoArgsConstructor
@Data
public class FmCuisineTypeResponseDTO {

    private Integer cuisineTypeId;

    private String cuisineTypeName;

}