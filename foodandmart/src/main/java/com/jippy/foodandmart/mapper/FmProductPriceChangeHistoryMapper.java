package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.entity.FmProductPriceChangeHistory;
import com.jippy.foodandmart.enums.FmPriceHistoryOperationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class FmProductPriceChangeHistoryMapper {

    public FmProductPriceChangeHistory toEntity(
            FmProductPriceSettingsRequestDto settings,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            Integer userId,
            FmPriceHistoryOperationType operationType) {

        if (settings == null) {
            throw new IllegalArgumentException("Price settings cannot be null");
        }

        if (oldPrice == null) {
            throw new IllegalArgumentException("Old price cannot be null");
        }

        if (newPrice == null) {
            throw new IllegalArgumentException("New price cannot be null");
        }

        if (operationType == null) {
            throw new IllegalArgumentException("Operation type cannot be null");
        }

        FmProductPriceChangeHistory history =
                new FmProductPriceChangeHistory();

        history.setOutletId(settings.getOutletId());
        history.setProductId(settings.getProductId());
        history.setProductVariantId(settings.getProductVariantId());

        history.setPriceType(settings.getPriceType());

        history.setStartDateTime(settings.getStartDateTime());
        history.setEndDateTime(settings.getEndDateTime());

        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);

        history.setOperationType(operationType);

        history.setLocationId(settings.getLocationId());
        history.setLocationType(settings.getLocationType());

        LocalDateTime now = LocalDateTime.now();

        Integer auditUser = userId != null
                ? userId
                : FmAppConstants.DEFAULT_CREATED_BY;

        history.setCreatedBy(auditUser);
        history.setCreatedAt(now);

        history.setUpdatedBy(auditUser);
        history.setUpdatedAt(now);

        return history;
    }
}