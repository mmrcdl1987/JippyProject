package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoCurrentOnlinePriceItemRequestDto {

    private Integer productId;

    private Integer variantOptionId;
}