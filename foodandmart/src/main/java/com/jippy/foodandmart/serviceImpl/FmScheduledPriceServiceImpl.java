package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.entity.FmProductPriceChangeHistory;
import com.jippy.foodandmart.entity.FmProductPriceSettings;
import com.jippy.foodandmart.enums.FmPriceHistoryOperationType;
import com.jippy.foodandmart.exception.PriceSettingsException;
import com.jippy.foodandmart.repository.FmPricingRepository;
import com.jippy.foodandmart.repository.FmProductPriceChangeHistoryRepository;
import com.jippy.foodandmart.repository.FmProductPriceSettingsRepository;
import com.jippy.foodandmart.service.IFmScheduledPriceCalculationService;
import com.jippy.foodandmart.service.IFmScheduledPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmScheduledPriceServiceImpl implements IFmScheduledPriceService {

    private final FmProductPriceSettingsRepository priceSettingsRepository;

    private final FmPricingRepository pricingRepository;

    private final FmProductPriceChangeHistoryRepository historyRepository;

    private final IFmScheduledPriceCalculationService priceCalculationService;

    /**
     * Applies all currently active scheduled prices.
     */
    @Override
    @Transactional
    public void applyScheduledPrices() {

        LocalDateTime currentDateTime = LocalDateTime.now();

        log.info("Scheduled price application started | currentDateTime={}", currentDateTime);

        List<FmProductPriceSettings> activeSettings = priceSettingsRepository.findActivePriceSettings(currentDateTime);

        if (activeSettings.isEmpty()) {

            log.info("No active scheduled price settings found | currentDateTime={}", currentDateTime);

            return;
        }

        log.info("Active scheduled price settings found | count={}", activeSettings.size());

        List<Integer> productIds = activeSettings.stream().map(FmProductPriceSettings::getProductId).distinct().toList();

        List<Integer> outletIds = activeSettings.stream().map(FmProductPriceSettings::getOutletId).distinct().toList();

        List<Object[]> outletCategoryRows = pricingRepository.findOutletCategoriesForScheduledPrices(productIds, outletIds);

        Map<String, Integer> outletCategoryMap = new HashMap<>();

        for (Object[] row : outletCategoryRows) {

            Integer productId = ((Number) row[0]).intValue();
            Integer outletId = ((Number) row[1]).intValue();
            Integer outletCategoryId = ((Number) row[2]).intValue();

            String key = productId + "_" + outletId;

            outletCategoryMap.put(key, outletCategoryId);
        }

        List<Integer> outletCategoryIds = outletCategoryRows.stream().map(row -> ((Number) row[2]).intValue()).distinct().toList();

        if (outletCategoryIds.isEmpty()) {

            log.warn("No outlet categories found for active scheduled price settings");

            return;
        }

        List<Object[]> currentPriceRows = pricingRepository.findCurrentPricesForScheduledUpdates(outletCategoryIds);

        Map<String, BigDecimal> currentPriceMap = new HashMap<>();

        for (Object[] row : currentPriceRows) {

            Integer productId = ((Number) row[0]).intValue();
            Integer outletCategoryId = ((Number) row[1]).intValue();

            Integer productVariantId = row[2] != null ? ((Number) row[2]).intValue() : null;

            BigDecimal onlinePrice = (BigDecimal) row[3];

            String key = buildPriceKey(productId, outletCategoryId, productVariantId);

            currentPriceMap.put(key, onlinePrice);
        }

        LocalDateTime minStartDateTime = activeSettings.stream().map(FmProductPriceSettings::getStartDateTime).min(LocalDateTime::compareTo).orElse(currentDateTime);

        LocalDateTime maxStartDateTime = activeSettings.stream().map(FmProductPriceSettings::getStartDateTime).max(LocalDateTime::compareTo).orElse(currentDateTime);

        List<FmProductPriceChangeHistory> applyHistories =
                historyRepository.findOperationHistoriesForScheduler(
                        FmPriceHistoryOperationType.APPLY,
                        currentDateTime,
                        currentDateTime
                );
        Set<String> alreadyAppliedKeys = applyHistories.stream().map(history -> buildHistoryKey(history.getOutletId(), history.getProductId(), history.getProductVariantId(), history.getStartDateTime(), history.getEndDateTime())).collect(Collectors.toSet());

        int appliedCount = 0;
        int skippedCount = 0;

        for (FmProductPriceSettings setting : activeSettings) {

            try {

                String outletCategoryKey = setting.getProductId() + "_" + setting.getOutletId();

                Integer outletCategoryId = outletCategoryMap.get(outletCategoryKey);

                if (outletCategoryId == null) {

                    log.warn("Outlet category not found | settingId={} | productId={} | outletId={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getOutletId());

                    skippedCount++;
                    continue;
                }

                String priceKey = buildPriceKey(setting.getProductId(), outletCategoryId, setting.getProductVariantId());

                BigDecimal currentPrice = currentPriceMap.get(priceKey);

                if (currentPrice == null) {

                    log.warn("Current online price not found | settingId={} | productId={} | outletCategoryId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getProductId(), outletCategoryId, setting.getProductVariantId());

                    skippedCount++;
                    continue;
                }

                boolean applied = applyPriceSetting(setting, outletCategoryId, currentPrice, alreadyAppliedKeys);

                if (applied) {
                    appliedCount++;
                } else {
                    skippedCount++;
                }

            } catch (Exception exception) {

                log.error("Failed to apply scheduled price | settingId={} | outletId={} | productId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), exception);

                skippedCount++;
            }
        }

        log.info("Scheduled price application completed | total={} | applied={} | skipped={}", activeSettings.size(), appliedCount, skippedCount);
    }

    private String buildHistoryKey(Integer outletId, Integer productId, Integer productVariantId, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        return outletId + "_" + productId + "_" + (productVariantId == null ? "NULL" : productVariantId) + "_" + startDateTime + "_" + endDateTime;
    }

    private String buildPriceKey(Integer productId, Integer outletCategoryId, Integer productVariantId) {
        return productId + "_" + outletCategoryId + "_" + (productVariantId == null ? "NULL" : productVariantId);
    }

    private boolean applyPriceSetting(FmProductPriceSettings setting, Integer outletCategoryId, BigDecimal currentPrice, Set<String> alreadyAppliedKeys) {

        log.info("Processing APPLY | settingId={} | outletId={} | productId={} | variantId={} | currentPrice={}", setting.getProductPriceSettingsId(), setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), currentPrice);

        String historyKey = buildHistoryKey(setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), setting.getStartDateTime(), setting.getEndDateTime());

        /*
         * APPLY histories are already bulk-loaded by
         * applyScheduledPrices().
         */
        if (alreadyAppliedKeys.contains(historyKey)) {

            log.debug("APPLY already processed | settingId={} | productId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getProductVariantId());

            return false;
        }

        BigDecimal newPrice = priceCalculationService.calculateNewPrice(currentPrice, setting.getPriceValue(), setting.getPriceType(), setting.getPriceAdjustmentType());

        log.debug("Scheduled price calculated | settingId={} | oldPrice={} | newPrice={}", setting.getProductPriceSettingsId(), currentPrice, newPrice);

        if (currentPrice.compareTo(newPrice) == 0) {

            log.debug("No price change required | settingId={} | currentPrice={}", setting.getProductPriceSettingsId(), currentPrice);

            return false;
        }

        int updatedRows = pricingRepository.updatePrice(setting.getProductId(), outletCategoryId, setting.getProductVariantId(), newPrice, setting.getCreatedBy(), setting.getCreatedBy());

        if (updatedRows == 0) {

            log.error("Online price update failed | settingId={} | productId={} | outletCategoryId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getProductId(), outletCategoryId, setting.getProductVariantId());

            throw new PriceSettingsException("Failed to update online product price");
        }

        FmProductPriceChangeHistory applyHistory = buildHistory(setting, currentPrice, newPrice, FmPriceHistoryOperationType.APPLY);

        historyRepository.save(applyHistory);

        /*
         * Prevent duplicate APPLY within the same scheduler execution.
         */
        alreadyAppliedKeys.add(historyKey);

        log.info("Scheduled price applied successfully | settingId={} | productId={} | variantId={} | oldPrice={} | newPrice={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getProductVariantId(), currentPrice, newPrice);

        return true;
    }

    /**
     * Restores all expired scheduled prices.
     */
    @Override
    @Transactional
    public void restoreExpiredPrices() {

        LocalDateTime currentDateTime = LocalDateTime.now();

        log.info("Expired price restoration started | currentDateTime={}", currentDateTime);

        List<FmProductPriceSettings> expiredSettings = priceSettingsRepository.findExpiredPriceSettings(currentDateTime);

        if (expiredSettings.isEmpty()) {

            log.info("No expired price settings found | currentDateTime={}", currentDateTime);

            return;
        }

        log.info("Expired price settings found | count={}", expiredSettings.size());

        /*
         * Bulk fetch APPLY histories.
         */
        List<FmProductPriceChangeHistory> applyHistories = historyRepository.findExpiredOperationHistoriesForScheduler(FmPriceHistoryOperationType.APPLY, currentDateTime);

        Map<String, FmProductPriceChangeHistory> applyHistoryMap = applyHistories.stream().collect(Collectors.toMap(history -> buildHistoryKey(history.getOutletId(), history.getProductId(), history.getProductVariantId(), history.getStartDateTime(), history.getEndDateTime()), history -> history, (existing, replacement) -> existing));

        /*
         * Bulk fetch RESTORE histories.
         */
        List<FmProductPriceChangeHistory> restoreHistories = historyRepository.findExpiredOperationHistoriesForScheduler(FmPriceHistoryOperationType.RESTORE, currentDateTime);

        Map<String, FmProductPriceChangeHistory> restoreHistoryMap = restoreHistories.stream().collect(Collectors.toMap(history -> buildHistoryKey(history.getOutletId(), history.getProductId(), history.getProductVariantId(), history.getStartDateTime(), history.getEndDateTime()), history -> history, (existing, replacement) -> existing));

        /*
         * Collect unique product and outlet IDs.
         */
        List<Integer> productIds = expiredSettings.stream().map(FmProductPriceSettings::getProductId).distinct().toList();

        List<Integer> outletIds = expiredSettings.stream().map(FmProductPriceSettings::getOutletId).distinct().toList();

        /*
         * Bulk fetch outlet categories.
         *
         * This replaces one outlet-category query per setting.
         */
        List<Object[]> outletCategoryRows = pricingRepository.findOutletCategoriesForScheduledPrices(productIds, outletIds);

        Map<String, Integer> outletCategoryMap = new HashMap<>();

        for (Object[] row : outletCategoryRows) {

            Integer productId = ((Number) row[0]).intValue();
            Integer outletId = ((Number) row[1]).intValue();
            Integer outletCategoryId = ((Number) row[2]).intValue();

            String key = productId + "_" + outletId;

            outletCategoryMap.put(key, outletCategoryId);
        }

        int restoredCount = 0;
        int skippedCount = 0;

        for (FmProductPriceSettings setting : expiredSettings) {

            try {

                String historyKey = buildHistoryKey(setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), setting.getStartDateTime(), setting.getEndDateTime());

                FmProductPriceChangeHistory applyHistory = applyHistoryMap.get(historyKey);

                if (applyHistory == null) {

                    log.warn("No APPLY history found for expired setting | settingId={}", setting.getProductPriceSettingsId());

                    skippedCount++;
                    continue;
                }

                FmProductPriceChangeHistory restoreHistory = restoreHistoryMap.get(historyKey);

                String outletCategoryKey = setting.getProductId() + "_" + setting.getOutletId();

                Integer outletCategoryId = outletCategoryMap.get(outletCategoryKey);

                if (outletCategoryId == null) {

                    log.warn("Outlet category not found | settingId={} | productId={} | outletId={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getOutletId());

                    skippedCount++;
                    continue;
                }

                boolean restored = restorePriceSetting(setting, applyHistory, restoreHistory, outletCategoryId);

                if (restored) {
                    restoredCount++;
                } else {
                    skippedCount++;
                }

            } catch (Exception exception) {

                log.error("Failed to restore expired price | settingId={} | outletId={} | productId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), exception);

                skippedCount++;
            }
        }

        log.info("Expired price restoration completed | total={} | restored={} | skipped={}", expiredSettings.size(), restoredCount, skippedCount);
    }

    /**
     * Restores the original price from APPLY history.
     */
    /**
     * Restores the original price from APPLY history.
     */
    /**
     * Restores the original price from APPLY history.
     */
    private boolean restorePriceSetting(FmProductPriceSettings setting, FmProductPriceChangeHistory applyHistory, FmProductPriceChangeHistory restoreHistory, Integer outletCategoryId) {

        log.info("Processing RESTORE | settingId={} | outletId={} | productId={} | variantId={} | outletCategoryId={}", setting.getProductPriceSettingsId(), setting.getOutletId(), setting.getProductId(), setting.getProductVariantId(), outletCategoryId);

        if (applyHistory == null) {

            log.warn("No APPLY history found for expired setting | settingId={} | productId={} | variantId={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getProductVariantId());

            return false;
        }

        if (applyHistory.getOldPrice() == null || applyHistory.getNewPrice() == null) {

            log.error("Invalid APPLY history | settingId={} | historyId={} | oldPrice={} | newPrice={}", setting.getProductPriceSettingsId(), applyHistory.getProductPriceChangeHistoryId(), applyHistory.getOldPrice(), applyHistory.getNewPrice());

            throw new PriceSettingsException("Invalid APPLY history for scheduled price restoration");
        }

        /*
         * RESTORE history is already bulk-fetched by restoreExpiredPrices().
         *
         * Do not execute another repository query here.
         */
        if (restoreHistory != null) {

            log.info("Price already restored | settingId={} | historyId={} | productId={} | variantId={}", setting.getProductPriceSettingsId(), restoreHistory.getProductPriceChangeHistoryId(), setting.getProductId(), setting.getProductVariantId());

            return false;
        }

        if (outletCategoryId == null) {

            log.error("Outlet category ID is null | settingId={} | outletId={} | productId={}", setting.getProductPriceSettingsId(), setting.getOutletId(), setting.getProductId());

            throw new PriceSettingsException("Outlet category not found for product");
        }

        BigDecimal originalPrice = applyHistory.getOldPrice();
        BigDecimal appliedPrice = applyHistory.getNewPrice();

        /*
         * Restore product_online_pricing to the original price
         * captured during APPLY.
         */
        int updatedRows = pricingRepository.updatePrice(setting.getProductId(), outletCategoryId, setting.getProductVariantId(), originalPrice, setting.getCreatedBy(), setting.getCreatedBy());

        if (updatedRows == 0) {

            log.error("Online price restoration failed | settingId={} | productId={} | outletCategoryId={} | variantId={} | originalPrice={}", setting.getProductPriceSettingsId(), setting.getProductId(), outletCategoryId, setting.getProductVariantId(), originalPrice);

            throw new PriceSettingsException("Failed to restore online product price");
        }

        /*
         * Save RESTORE history:
         *
         * scheduled price -> original price
         */
        FmProductPriceChangeHistory newRestoreHistory = buildHistory(setting, appliedPrice, originalPrice, FmPriceHistoryOperationType.RESTORE);

        historyRepository.save(newRestoreHistory);

        log.info("Scheduled price restored successfully | settingId={} | productId={} | variantId={} | appliedPrice={} | originalPrice={}", setting.getProductPriceSettingsId(), setting.getProductId(), setting.getProductVariantId(), appliedPrice, originalPrice);

        return true;
    }

    /**
     * Creates history for APPLY or RESTORE operation.
     */
    private FmProductPriceChangeHistory buildHistory(FmProductPriceSettings setting, BigDecimal oldPrice, BigDecimal newPrice, FmPriceHistoryOperationType operationType) {

        FmProductPriceChangeHistory history = new FmProductPriceChangeHistory();

        history.setOutletId(setting.getOutletId());
        history.setProductId(setting.getProductId());
        history.setProductVariantId(setting.getProductVariantId());

        history.setPriceType(setting.getPriceType());

        history.setStartDateTime(setting.getStartDateTime());
        history.setEndDateTime(setting.getEndDateTime());

        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);

        /*
         * IMPORTANT:
         * Explicitly identify whether this is APPLY
         * or RESTORE.
         */
        history.setOperationType(operationType);

        history.setLocationId(setting.getLocationId());
        history.setLocationType(setting.getLocationType());

        Integer userId = operationType == FmPriceHistoryOperationType.RESTORE ? setting.getUpdatedBy() : setting.getCreatedBy();

        history.setCreatedBy(userId);
        history.setCreatedAt(LocalDateTime.now());

        return history;
    }
}