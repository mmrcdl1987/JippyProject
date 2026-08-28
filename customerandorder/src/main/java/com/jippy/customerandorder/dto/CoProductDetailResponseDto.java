package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoProductDetailResponseDto {

    private Integer productId;

    private String productName;

    private BigDecimal merchantPrice;

    private String imageLink;

    private Boolean hasProductVariants;

    private List<CoProductVariantGroupDetailResponseDto> variantGroups =
            new ArrayList<>();
}