package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FmMerchantSettlementProductDetailsDto {

    private String productName;

    private List<FmMerchantSettlementVariantDetailsDto> variants;
}