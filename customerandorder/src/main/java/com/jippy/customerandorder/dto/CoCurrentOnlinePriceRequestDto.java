package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoCurrentOnlinePriceRequestDto {

    private Integer outletId;

    private List<CoCurrentOnlinePriceItemRequestDto> items;
}