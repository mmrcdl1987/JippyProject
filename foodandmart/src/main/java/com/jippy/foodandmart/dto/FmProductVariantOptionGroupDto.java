package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FmProductVariantOptionGroupDto {

    @NotNull(message = "Product Variant Group Id is required")
    private Integer productVariantGroupsId;

    @Valid
    private List<FmProductVariantOptionRequestDto> options;
}