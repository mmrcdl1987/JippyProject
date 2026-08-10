package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmProductResponseDto;
import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class FmPricingMapper {

    public FmProductResponseDto map(Object[] row) {

        return new FmProductResponseDto(((Number) row[0]).intValue(), (String) row[1], (BigDecimal) row[2], row[3] != null ? (BigDecimal) row[3] : null);
    }

    /**
     * Creates a product_online_pricing entity.
     * <p>
     * productVariantId = null
     * -> Base product price
     * <p>
     * productVariantId != null
     * -> MAIN / ADD variant price
     */
    public FmProductOnlinePricing toEntity(Integer productId, Integer outletCategoryId, Integer productVariantId, BigDecimal onlinePrice) {

        FmProductOnlinePricing entity = new FmProductOnlinePricing();

        entity.setProductId(productId);
        entity.setOutletCategoryId(outletCategoryId);
        entity.setProductVariantId(productVariantId);
        entity.setOnlinePrice(onlinePrice);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        entity.setIsApproved(true);
        entity.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);

        return entity;
    }
}