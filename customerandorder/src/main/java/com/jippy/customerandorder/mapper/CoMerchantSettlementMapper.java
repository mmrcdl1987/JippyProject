package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoFmOutletDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementOutletDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementProductDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementResponseDto;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.projection.CoOrderSettlementProjection;

public class CoMerchantSettlementMapper {

    //     Map order projection to response dto
    public static CoMerchantSettlementResponseDto toSettlementResponseDto(CoOrderSettlementProjection order) {

        CoMerchantSettlementResponseDto dto = new CoMerchantSettlementResponseDto();

        dto.setOrderId(order.getOrderId());

        dto.setOutletId(order.getOutletId());

        dto.setOrderStatus(order.getOrderStatus());

        dto.setCreatedAt(order.getCreatedAt());

        dto.setProductsTotalPrice(order.getTotalPrice());

        return dto;
    }


    //     Map order item to product dto
    public static CoMerchantSettlementProductDto toProductDto(CoOrderItem item) {

        CoMerchantSettlementProductDto dto = new CoMerchantSettlementProductDto();

        dto.setProductId(item.getProductId());

        dto.setQuantity(item.getQuantity());

        return dto;
    }


    //     Map outlet settlement details
    public static CoMerchantSettlementOutletDto toOutletSettlementDto(

            CoOrderSettlementProjection order,

            CoFmOutletDto outletDto

    ) {

        CoMerchantSettlementOutletDto dto = new CoMerchantSettlementOutletDto();

        dto.setOutletId(order.getOutletId());

        dto.setOutletName(outletDto.getOutletName());

        dto.setOutletPhone(outletDto.getOutletPhone());

//        dto.setAreaName(areaName);

        dto.setAreaName(outletDto.getAreaName());

        dto.setSettlementAmount(order.getTotalPrice());

        return dto;
    }

}