package com.jippy.foodandmart.service;

import com.jippy.foodandmart.enums.FmPriceAdjustmentType;
import com.jippy.foodandmart.enums.FmPriceType;

import java.math.BigDecimal;

public interface IFmScheduledPriceCalculationService {

    BigDecimal calculateNewPrice(
            BigDecimal currentPrice,
            BigDecimal priceValue,
            FmPriceType priceType,
            FmPriceAdjustmentType priceAdjustmentType
    );
}