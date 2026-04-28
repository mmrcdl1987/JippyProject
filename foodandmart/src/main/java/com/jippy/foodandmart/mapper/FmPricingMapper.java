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
        return new FmProductResponseDto(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (BigDecimal) row[2],
                (BigDecimal) row[3]
        );
    }

    public FmProductOnlinePricing toEntity(Integer productId,
                                           Integer outletCategoryId,
                                           BigDecimal price) {

        FmProductOnlinePricing ProductOnlinePricingEntity = new FmProductOnlinePricing();

        ProductOnlinePricingEntity.setProductId(productId);
        ProductOnlinePricingEntity.setOutletCategoryId(outletCategoryId);
        ProductOnlinePricingEntity.setOnlinePrice(price);

        ProductOnlinePricingEntity.setCreatedAt(LocalDateTime.now());
        ProductOnlinePricingEntity.setUpdatedAt(LocalDateTime.now());

        ProductOnlinePricingEntity.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);
        ProductOnlinePricingEntity.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        ProductOnlinePricingEntity.setIsApproved(true);
        ProductOnlinePricingEntity.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);

        return ProductOnlinePricingEntity;
    }
}