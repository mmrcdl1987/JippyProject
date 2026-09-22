package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.projection.CoMerchantSettlementOrderDetailsProjection;
import com.jippy.customerandorder.projection.CoOrderSettlementProjection;
import com.jippy.customerandorder.projection.CoOutletSettlementProjection;

import java.util.ArrayList;
import java.util.List;

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

    public static List<CoOutletSettlementDto> toOutletSettlementDtoList(List<CoOutletSettlementProjection> projections) {

        List<CoOutletSettlementDto> outletSettlements = new ArrayList<>();

        if (projections == null || projections.isEmpty()) {
            return outletSettlements;
        }

        for (CoOutletSettlementProjection projection : projections) {

            CoOutletSettlementDto dto = new CoOutletSettlementDto();

            dto.setOutletId(projection.getOutletId());

            dto.setMerchantTotalPrice(projection.getMerchantTotalPrice());

            dto.setPromotionDeductedAmount(projection.getPromotionDeductedAmount());

            outletSettlements.add(dto);
        }

        return outletSettlements;
    }

    /**
     * Maps one CO settlement order-details projection
     * to the CO settlement order-item DTO.
     * <p>
     * One projection record represents one order item.
     *
     * @param projection CO settlement order-details projection
     * @return CO order-item settlement DTO
     */
    public static CoMerchantSettlementOrderItemDto toOrderItemSettlementDto(CoMerchantSettlementOrderDetailsProjection projection) {

        CoMerchantSettlementOrderItemDto dto = new CoMerchantSettlementOrderItemDto();

        // Order information
        dto.setOrderId(projection.getOrderId());
        dto.setCreatedAt(projection.getCreatedAt());

        // Product and variant IDs.
        // FM will use these IDs to fetch product/variant details.
        dto.setProductId(projection.getProductId());
        dto.setVariantOptionId(projection.getVariantOptionId());

        // Quantity ordered
        dto.setQuantity(projection.getQuantity());

        // Merchant price before promotion deduction
        dto.setMerchantTotalPrice(projection.getMerchantTotalPrice());

        // Promotion amount from CO.
        // Service logic will determine whether this is
        // applicable based on discountType.
        dto.setPromotionDeductedAmount(projection.getPromotionDeductedAmount());

        // Discount type is required by FM/service logic
        // to identify MERCHANT_PROMOTION.
        dto.setDiscountType(projection.getDiscountType());

        return dto;
    }

    /**
     * Maps multiple CO settlement projections
     * to a list of CO settlement order-item DTOs.
     * <p>
     * Manual loop is intentionally used instead of
     * streams, maps or builders.
     *
     * @param projections list of CO settlement projections
     * @return list of CO settlement order-item DTOs
     */
    public static List<CoMerchantSettlementOrderItemDto> toOrderItemSettlementDtoList(List<CoMerchantSettlementOrderDetailsProjection> projections) {

        List<CoMerchantSettlementOrderItemDto> dtoList = new ArrayList<>();

        if (projections == null || projections.isEmpty()) {
            return dtoList;
        }

        for (CoMerchantSettlementOrderDetailsProjection projection : projections) {

            CoMerchantSettlementOrderItemDto dto = toOrderItemSettlementDto(projection);

            dtoList.add(dto);
        }

        return dtoList;
    }
//    ===============================================================================
//    ===============================================================================
public static CoMerchantSettlementOrderItemDto
toMerchantSettlementOrderItemDto(
        CoMerchantSettlementOrderDetailsProjection projection) {

    CoMerchantSettlementOrderItemDto dto =
            new CoMerchantSettlementOrderItemDto();

    dto.setOrderId(projection.getOrderId());
    dto.setCreatedAt(projection.getCreatedAt());
    dto.setProductId(projection.getProductId());
    dto.setVariantOptionId(projection.getVariantOptionId());
    dto.setQuantity(projection.getQuantity());
    dto.setMerchantTotalPrice(projection.getMerchantTotalPrice());
    dto.setPromotionDeductedAmount(
            projection.getPromotionDeductedAmount()
    );
    dto.setDiscountType(projection.getDiscountType());

    return dto;
}
}