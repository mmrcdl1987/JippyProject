package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmOutletsResponseDto;
import com.jippy.foodandmart.projections.FmOutletSettlementProjection;

public class FmMerchantSettlementMapper {
    // for mapping outlet details in settlement process in Customer and Order microservice
    public static FmOutletsResponseDto toOutletAndAddressAreaResponseDto(FmOutletSettlementProjection outlet) {

        FmOutletsResponseDto dto = new FmOutletsResponseDto();

        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());
        dto.setOutletPhone(outlet.getOutletPhone());
        dto.setAreaId(outlet.getAreaId());
        dto.setAreaName(outlet.getAreaName());

        return dto;
    }
}
