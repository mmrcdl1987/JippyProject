package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.enums.FmPriceAdjustmentType;
import com.jippy.foodandmart.enums.FmPriceType;
import com.jippy.foodandmart.exception.PriceSettingsException;
import com.jippy.foodandmart.service.IFmScheduledPriceCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class FmScheduledPriceCalculationServiceImpl implements IFmScheduledPriceCalculationService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final int PRICE_SCALE = 2;
    private static final int CALCULATION_SCALE = 4;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculateNewPrice(BigDecimal currentPrice, BigDecimal priceValue, FmPriceType priceType, FmPriceAdjustmentType priceAdjustmentType) {

        validateInputs(currentPrice, priceValue, priceType, priceAdjustmentType);

        BigDecimal newPrice;

        if (FmPriceType.FLAT.equals(priceType)) {

            newPrice = calculateFlatPrice(currentPrice, priceValue, priceAdjustmentType);

        } else if (FmPriceType.PERCENTAGE.equals(priceType)) {

            newPrice = calculatePercentagePrice(currentPrice, priceValue, priceAdjustmentType);

        } else {

            log.error("Unsupported price type | priceType={}", priceType);

            throw new PriceSettingsException("Unsupported price type: " + priceType);
        }

        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {

            log.error("Calculated price cannot be negative | currentPrice={} | value={} | priceType={} | adjustmentType={} | calculatedPrice={}", currentPrice, priceValue, priceType, priceAdjustmentType, newPrice);

            throw new PriceSettingsException("Calculated price cannot be negative");
        }

        newPrice = newPrice.setScale(PRICE_SCALE, ROUNDING_MODE);

        log.debug("Price calculated | currentPrice={} | value={} | priceType={} | adjustmentType={} | newPrice={}", currentPrice, priceValue, priceType, priceAdjustmentType, newPrice);

        return newPrice;
    }

    private BigDecimal calculateFlatPrice(BigDecimal currentPrice, BigDecimal priceValue, FmPriceAdjustmentType adjustmentType) {

        if (FmPriceAdjustmentType.INCREASE.equals(adjustmentType)) {

            return currentPrice.add(priceValue);

        } else if (FmPriceAdjustmentType.DECREASE.equals(adjustmentType)) {

            return currentPrice.subtract(priceValue);
        }

        throw new PriceSettingsException("Unsupported price adjustment type: " + adjustmentType);
    }

    private BigDecimal calculatePercentagePrice(BigDecimal currentPrice, BigDecimal priceValue, FmPriceAdjustmentType adjustmentType) {

        BigDecimal adjustmentAmount = currentPrice.multiply(priceValue).divide(ONE_HUNDRED, CALCULATION_SCALE, ROUNDING_MODE);

        if (FmPriceAdjustmentType.INCREASE.equals(adjustmentType)) {

            return currentPrice.add(adjustmentAmount);

        } else if (FmPriceAdjustmentType.DECREASE.equals(adjustmentType)) {

            return currentPrice.subtract(adjustmentAmount);
        }

        throw new PriceSettingsException("Unsupported price adjustment type: " + adjustmentType);
    }

    private void validateInputs(BigDecimal currentPrice, BigDecimal priceValue, FmPriceType priceType, FmPriceAdjustmentType priceAdjustmentType) {

        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new PriceSettingsException("Current price cannot be null or negative");
        }

        if (priceValue == null || priceValue.compareTo(BigDecimal.ZERO) <= 0) {

            throw new PriceSettingsException("Price value must be greater than zero");
        }

        if (priceType == null) {

            throw new PriceSettingsException("Price type is required");
        }

        if (priceAdjustmentType == null) {

            throw new PriceSettingsException("Price adjustment type is required");
        }

        if (FmPriceType.PERCENTAGE.equals(priceType) && priceValue.compareTo(ONE_HUNDRED) > 0) {

            throw new PriceSettingsException("Percentage value cannot be greater than 100");
        }
    }
}