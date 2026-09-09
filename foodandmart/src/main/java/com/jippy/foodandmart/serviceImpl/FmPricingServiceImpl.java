package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import com.jippy.foodandmart.entity.FmProductPriceChangeHistory;
import com.jippy.foodandmart.enums.FmPriceHistoryOperationType;
import com.jippy.foodandmart.enums.FmPriceType;
import com.jippy.foodandmart.exception.PricingException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.mapper.FmPricingMapper;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmPricingRepository;
import com.jippy.foodandmart.repository.FmProductPriceChangeHistoryRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.FmProductVariantOptionRepository;
import com.jippy.foodandmart.service.IPricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmPricingServiceImpl implements IPricingService {

    private static final int PRICE_SCALE = 2;

    private final FmOutletRepository outletRepo;
    private final FmProductRepository productRepo;
    private final FmPricingRepository pricingRepo;
    private final FmPricingMapper pricingMapper;

    /*
     * Kept because existing pricing functionality may use Division service.
     * Bulk pricing itself validates FLAT / PERCENTAGE locally.
     */
    private final DivisionFeignClient divisionFeignClient;

    private final FmProductMapper productMapper;

    /*
     * Kept because existing individual pricing functionality supports variants.
     */
    private final FmProductVariantOptionRepository variantOptionRepo;

    private final FmProductPriceChangeHistoryRepository priceHistoryRepository;

    /*
     * Existing cache invalidation functionality.
     */
    private final CacheInvalidateServiceImpl cacheInvalidateService;


    // ============================================================
    // GET OUTLETS BASED ON APPROVAL STATUS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<FmOutletDto> getOutlets(Integer areaId, boolean isApproved, String search) {

        log.info("SERVICE START | GET_OUTLETS | areaId={} | isApproved={} | search={}", areaId, isApproved, search);

        if (areaId == null) {
            log.error("AreaId is null");
            throw new PricingException("AreaId cannot be null");
        }

        if (search != null && search.isBlank()) {
            search = null;
        }

        List<FmOutlet> outlets = isApproved ? outletRepo.findApprovedOutlets(areaId, FmAppConstants.TYPE_OUTLET, search) : outletRepo.findUnapprovedOutlets(areaId, FmAppConstants.TYPE_OUTLET, search);

        if (outlets == null || outlets.isEmpty()) {
            log.warn("No outlets found | areaId={} | isApproved={}", areaId, isApproved);

            throw new PricingException("No outlets found");
        }

        log.info("SERVICE END | GET_OUTLETS | count={}", outlets.size());

        return outlets.stream().map(o -> new FmOutletDto(o.getOutletId(), o.getOutletName())).toList();
    }


    // ============================================================
    // GET PRODUCTS BASED ON OUTLET IDS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<FmProductResponseDto> getProducts(List<Integer> outletIds, boolean isApproved) {

        log.info("SERVICE START | GET_PRODUCTS | outletIds={} | isApproved={}", outletIds, isApproved);

        if (outletIds == null || outletIds.isEmpty()) {
            throw new PricingException("OutletIds cannot be empty");
        }

        List<Object[]> rows = isApproved ? productRepo.findProducts(outletIds) : productRepo.findProductsWithoutPricing(outletIds);

        if (rows == null || rows.isEmpty()) {
            log.warn("No products found | outletIds={}", outletIds);

            throw new PricingException("No products found");
        }

        log.info("SERVICE END | GET_PRODUCTS | count={}", rows.size());

        return rows.stream().map(pricingMapper::map).toList();
    }


    // ============================================================
    // INDIVIDUAL PRODUCT / VARIANT PRICE UPDATE
    // ============================================================

    @Override
    @Transactional
    public void updatePrices(FmPriceUpdateRequestDto dto, boolean isApproved) {

        /*
         * IMPORTANT:
         * Validate DTO before accessing any property.
         */
        if (dto == null) {
            log.error("SERVICE FAILED | PRICE_UPDATE | request is null");
            throw new PricingException("Price update request cannot be null");
        }

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new PricingException("OutletIds cannot be empty");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {

            throw new PricingException("Price update items cannot be empty");
        }

        log.info("SERVICE START | UPDATE_PRICES | outlets={} | items={} | isApproved={}", dto.getOutletIds(), dto.getItems().size(), isApproved);

        LocalDateTime now = LocalDateTime.now();

        for (Integer outletId : dto.getOutletIds()) {

            if (outletId == null || outletId <= 0) {
                throw new PricingException("Invalid outletId: " + outletId);
            }

            for (FmPriceUpdateRequestDto.Item item : dto.getItems()) {

                if (item == null) {
                    throw new PricingException("Price update item cannot be null");
                }

                Integer productId = item.getProductId();
                Integer productVariantId = item.getProductVariantId();
                BigDecimal newPrice = item.getNewPrice();

                if (productId == null || productId <= 0) {
                    throw new PricingException("Invalid productId: " + productId);
                }

                if (newPrice == null) {
                    throw new PricingException("New price cannot be null");
                }

                if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new PricingException("New price must be greater than zero");
                }

                newPrice = newPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

                log.info("PROCESS PRICE | outletId={} | productId={} | variantId={} | newPrice={}", outletId, productId, productVariantId, newPrice);

                // ----------------------------------------------------
                // FIND OUTLET CATEGORY
                // ----------------------------------------------------

                Integer outletCategoryId = pricingRepo.findOutletCategoryIdByProductAndOutlet(productId, outletId).orElseThrow(() -> new PricingException("Product " + productId + " is not available for outlet " + outletId));

                // ----------------------------------------------------
                // GET CURRENT PRICE
                // ----------------------------------------------------

                BigDecimal oldPrice = pricingRepo.findCurrentPriceForScheduledUpdate(productId, outletCategoryId, productVariantId).orElse(BigDecimal.ZERO);

                oldPrice = oldPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

                log.info("PRICE COMPARISON | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}", outletId, productId, productVariantId, oldPrice, newPrice);

                // ----------------------------------------------------
                // NO CHANGE
                // ----------------------------------------------------

                if (oldPrice.compareTo(newPrice) == 0) {

                    log.info("PRICE NOT CHANGED | outletId={} | productId={} | variantId={} | price={}", outletId, productId, productVariantId, newPrice);

                    continue;
                }

                // ----------------------------------------------------
                // UPSERT ONLINE PRICE
                // ----------------------------------------------------

                upsertPrice(productId, outletCategoryId, productVariantId, newPrice);

                // ----------------------------------------------------
                // SAVE HISTORY
                // ----------------------------------------------------

                savePriceChangeHistory(outletId, productId, productVariantId, oldPrice, newPrice, now);

                // ----------------------------------------------------
                // INVALIDATE CACHE
                // ----------------------------------------------------

                cacheInvalidateService.invalidateCache(outletId);

                log.info("PRICE UPDATED | HISTORY SAVED | CACHE INVALIDATED | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}", outletId, productId, productVariantId, oldPrice, newPrice);
            }
        }

        // ------------------------------------------------------------
        // APPROVE OUTLETS FOR UNAPPROVED FLOW
        // ------------------------------------------------------------

        if (!isApproved) {

            outletRepo.approveOutlets(dto.getOutletIds());

            log.info("Outlets approved | outlets={}", dto.getOutletIds());
        }

        log.info("SERVICE END | UPDATE_PRICES");
    }


    // ============================================================
    // SAVE PRICE CHANGE HISTORY
    // ============================================================

    private void savePriceChangeHistory(Integer outletId, Integer productId, Integer productVariantId, BigDecimal oldPrice, BigDecimal newPrice, LocalDateTime now) {

        FmProductPriceChangeHistory history = new FmProductPriceChangeHistory();

        history.setOutletId(outletId);
        history.setProductId(productId);
        history.setProductVariantId(productVariantId);

        /*
         * Individual update is always a FLAT direct update.
         */
        history.setPriceType(FmPriceType.FLAT);

        history.setStartDateTime(null);
        history.setEndDateTime(null);

        history.setOldPrice(oldPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP));

        history.setNewPrice(newPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP));

        history.setOperationType(FmPriceHistoryOperationType.UPDATE);

        history.setLocationId(outletId);
        history.setLocationType(FmAppConstants.ADDRESS_TYPE_OUTLET);

        history.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        history.setCreatedAt(now);

        history.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        history.setUpdatedAt(now);

        priceHistoryRepository.save(history);

        log.info("PRICE HISTORY SAVED | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}", outletId, productId, productVariantId, oldPrice, newPrice);
    }


    // ============================================================
    // BULK UPDATE PRICES
    //
    // PRODUCT LEVEL ONLY
    //
    // SUPPORTED:
    //
    // FLAT       + INCREASE
    // FLAT       + DECREASE
    // PERCENTAGE + INCREASE
    // PERCENTAGE + DECREASE
    //
    // IMPORTANT:
    // Existing online price is used as the base for subsequent
    // increase/decrease operations.
    //
    // Example:
    //
    // merchantPrice = 100
    //
    // First increase 10%
    // onlinePrice = 110
    //
    // Next increase 10%
    // onlinePrice = 121
    //
    // Next decrease 10%
    // onlinePrice = 108.90
    //
    // No variant processing in this bulk flow.
    // ============================================================

    @Override
    @Transactional
    public void bulkUpdatePrices(FmBulkPriceUpdateRequestDto dto, boolean isApproved) {

        /*
         * ----------------------------------------------------------
         * STEP 1: VALIDATE REQUEST
         * ----------------------------------------------------------
         */

        validateRequest(dto);

        validateBulkPriceModel(dto.getPriceModel());

        validateBulkHistoryFields(dto);

        LocalDateTime changeTime = LocalDateTime.now();

        log.info("BULK PRICE START | outlets={} | priceModel={} | value={} | priceType={} | operationType={} | isApproved={}", dto.getOutletIds(), dto.getPriceModel(), dto.getValue(), dto.getPriceType(), dto.getOperationType(), isApproved);

        /*
         * ----------------------------------------------------------
         * STEP 2:
         * FETCH ALL ACTIVE PRODUCTS
         *
         * Repository result:
         *
         * [0] productId
         * [1] productName
         * [2] merchantPrice
         * [3] outletCategoryId
         * [4] outletId
         * ----------------------------------------------------------
         */

        List<Object[]> productRows = productRepo.findProductsForBulkPricing(dto.getOutletIds());

        if (productRows == null || productRows.isEmpty()) {

            log.warn("BULK PRICE | No active products found | outlets={}", dto.getOutletIds());

            if (!isApproved) {

                outletRepo.approveOutlets(dto.getOutletIds());
            }

            return;
        }

        log.info("BULK PRICE | Products fetched={} | outlets={}", productRows.size(), dto.getOutletIds());

        /*
         * ----------------------------------------------------------
         * STEP 3:
         * EXTRACT OUTLET CATEGORY IDS
         * ----------------------------------------------------------
         */

        List<Integer> outletCategoryIds = productRows.stream().map(row -> ((Number) row[3]).intValue()).distinct().toList();

        /*
         * ----------------------------------------------------------
         * STEP 4:
         * FETCH EXISTING ONLINE PRICING
         * ----------------------------------------------------------
         */

        List<Object[]> existingPricingRows = pricingRepo.findExistingBulkPricing(outletCategoryIds);

        if (existingPricingRows == null) {
            existingPricingRows = Collections.emptyList();
        }

        log.info("BULK PRICE | Existing pricing rows={}", existingPricingRows.size());

        /*
         * ----------------------------------------------------------
         * STEP 5:
         * BUILD EXISTING PRICING ID LIST
         * ----------------------------------------------------------
         */

        List<Integer> existingPricingIds = existingPricingRows.stream().filter(row -> row != null && row.length > 0 && row[0] != null).map(row -> ((Number) row[0]).intValue()).distinct().toList();

        /*
         * ----------------------------------------------------------
         * STEP 6:
         * LOAD EXISTING PRICING ENTITIES
         * ----------------------------------------------------------
         */

        Map<Integer, FmProductOnlinePricing> existingPricingById;

        if (existingPricingIds.isEmpty()) {

            existingPricingById = new HashMap<>();

        } else {

            existingPricingById = pricingRepo.findAllById(existingPricingIds).stream().collect(Collectors.toMap(FmProductOnlinePricing::getProductOnlinePricingId, Function.identity()));
        }

        /*
         * ----------------------------------------------------------
         * STEP 7:
         * BUILD PRODUCT-LEVEL PRICING LOOKUP
         *
         * Key:
         *
         * productId
         * +
         * outletCategoryId
         * +
         * variantId
         *
         * variantId = null
         * means BASE PRODUCT PRICE.
         *
         * IMPORTANT:
         * Variant records are ignored by bulk pricing.
         * ----------------------------------------------------------
         */

        Map<PricingKey, FmProductOnlinePricing> existingPricingMap = new HashMap<>();

        for (Object[] row : existingPricingRows) {

            if (row == null || row.length < 4 || row[0] == null || row[1] == null || row[2] == null) {

                continue;
            }

            Integer pricingId = ((Number) row[0]).intValue();

            Integer productId = ((Number) row[1]).intValue();

            Integer outletCategoryId = ((Number) row[2]).intValue();

            /*
             * If repository returns variant ID as fourth column,
             * use it. Otherwise product-level pricing is null.
             */
            Integer variantId = row[3] == null ? null : ((Number) row[3]).intValue();

            FmProductOnlinePricing entity = existingPricingById.get(pricingId);

            if (entity != null && entity.getProductVariantId() == null && variantId == null) {

                existingPricingMap.put(new PricingKey(productId, outletCategoryId, null), entity);
            }
        }

        /*
         * ----------------------------------------------------------
         * STEP 8:
         * PREPARE BATCH LISTS
         * ----------------------------------------------------------
         */

        List<FmProductOnlinePricing> pricingEntitiesToSave = new ArrayList<>();

        List<FmProductPriceChangeHistory> historyEntitiesToSave = new ArrayList<>();

        int productCount = 0;
        int insertCount = 0;
        int updateCount = 0;
        int skippedCount = 0;

        /*
         * ----------------------------------------------------------
         * STEP 9:
         * PROCESS PRODUCTS
         * ----------------------------------------------------------
         */

        for (Object[] row : productRows) {

            if (row == null || row.length < 5) {

                throw new PricingException("Invalid product data returned from database");
            }

            Integer productId = ((Number) row[0]).intValue();

            /*
             * IMPORTANT:
             *
             * Repository returns:
             *
             * [0] productId
             * [1] productName
             * [2] merchantPrice
             * [3] outletCategoryId
             * [4] outletId
             *
             * Therefore outletId MUST be row[4].
             */
            Integer outletId = ((Number) row[4]).intValue();

            BigDecimal merchantPrice = toBigDecimal(row[2]);

            Integer outletCategoryId = ((Number) row[3]).intValue();

            if (merchantPrice == null) {

                throw new PricingException("Merchant price cannot be null" + " | productId=" + productId + " | outletId=" + outletId);
            }

            merchantPrice = merchantPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

            PricingKey productKey = new PricingKey(productId, outletCategoryId, null);

            FmProductOnlinePricing existingProductPricing = existingPricingMap.get(productKey);

            /*
             * ------------------------------------------------------
             * EXISTING ONLINE PRICE
             * ------------------------------------------------------
             */

            BigDecimal oldPrice;

            if (existingProductPricing == null) {

                oldPrice = BigDecimal.ZERO;

            } else {

                oldPrice = existingProductPricing.getOnlinePrice();

                if (oldPrice == null) {
                    oldPrice = BigDecimal.ZERO;
                }
            }

            oldPrice = oldPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

            /*
             * ------------------------------------------------------
             * RESOLVE BASE PRICE
             *
             * First time:
             * online price null / 0
             * -> merchant price
             *
             * Existing:
             * online price > 0
             * -> existing online price
             * ------------------------------------------------------
             */

            BigDecimal basePrice = resolveBasePrice(merchantPrice, oldPrice);

            /*
             * ------------------------------------------------------
             * CALCULATE NEW PRICE
             * ------------------------------------------------------
             */

            BigDecimal newPrice = calculateFinalPrice(basePrice, dto.getPriceModel(), dto.getValue(), dto.getOperationType());

            log.info("PRODUCT PRICE | outletId={} | productId={} | merchantPrice={} | oldOnlinePrice={} | basePrice={} | priceModel={} | value={} | operationType={} | newOnlinePrice={}", outletId, productId, merchantPrice, oldPrice, basePrice, dto.getPriceModel(), dto.getValue(), dto.getOperationType(), newPrice);

            /*
             * ------------------------------------------------------
             * NO CHANGE
             * ------------------------------------------------------
             */

            if (existingProductPricing != null && oldPrice.compareTo(newPrice) == 0) {

                log.info("BULK PRICE | Price unchanged | outletId={} | productId={} | price={}", outletId, productId, oldPrice);

                skippedCount++;
                continue;
            }

            /*
             * ------------------------------------------------------
             * UPDATE EXISTING PRICE
             * ------------------------------------------------------
             */

            if (existingProductPricing != null) {

                updateEntity(existingProductPricing, newPrice, changeTime, isApproved);

                pricingEntitiesToSave.add(existingProductPricing);

                updateCount++;

            } else {

                /*
                 * --------------------------------------------------
                 * INSERT NEW PRODUCT ONLINE PRICE
                 * --------------------------------------------------
                 */

                FmProductOnlinePricing newPricing = pricingMapper.toEntity(productId, outletCategoryId, null, newPrice);

                newPricing.setProductVariantId(null);

                newPricing.setOnlinePrice(newPrice);

                newPricing.setIsApproved(isApproved);

                newPricing.setCreatedAt(changeTime);

                newPricing.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

                if (isApproved) {

                    newPricing.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);
                }

                pricingEntitiesToSave.add(newPricing);

                insertCount++;
            }

            /*
             * ------------------------------------------------------
             * SAVE HISTORY IN MEMORY
             * ------------------------------------------------------
             */

            historyEntitiesToSave.add(buildPriceHistory(dto, outletId, productId, null, oldPrice, newPrice, changeTime));

            productCount++;
        }

        /*
         * ----------------------------------------------------------
         * STEP 10:
         * SAVE ONLINE PRICING
         * ----------------------------------------------------------
         */

        if (!pricingEntitiesToSave.isEmpty()) {

            pricingRepo.saveAll(pricingEntitiesToSave);

            log.info("BULK PRICE | Pricing persisted | total={} | inserts={} | updates={}", pricingEntitiesToSave.size(), insertCount, updateCount);
        }

        /*
         * ----------------------------------------------------------
         * STEP 11:
         * SAVE PRICE HISTORY
         * ----------------------------------------------------------
         */

        if (!historyEntitiesToSave.isEmpty()) {

            priceHistoryRepository.saveAll(historyEntitiesToSave);

            log.info("BULK PRICE | History persisted | total={}", historyEntitiesToSave.size());
        }

        /*
         * ----------------------------------------------------------
         * STEP 12:
         * INVALIDATE CACHE FOR AFFECTED OUTLETS
         * ----------------------------------------------------------
         */

        if (!pricingEntitiesToSave.isEmpty()) {

            for (Integer outletId : dto.getOutletIds()) {

                cacheInvalidateService.invalidateCache(outletId);
            }

            log.info("BULK PRICE | Cache invalidated | outlets={}", dto.getOutletIds());
        }

        /*
         * ----------------------------------------------------------
         * STEP 13:
         * APPROVE OUTLETS
         * ----------------------------------------------------------
         */

        if (!isApproved) {

            outletRepo.approveOutlets(dto.getOutletIds());

            log.info("BULK PRICE | Outlets approved | outlets={}", dto.getOutletIds());
        }

        log.info("BULK PRICE END | processed={} | inserts={} | updates={} | skipped={} | history={}", productCount, insertCount, updateCount, skippedCount, historyEntitiesToSave.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FmCurrentOnlinePriceResponse> getCurrentOnlinePrices(FmCurrentOnlinePriceRequest request) {

        log.info("[CURRENT-ONLINE-PRICE] START | outletId={} | itemCount={}", request.getOutletId(), request.getItems().size());

        /*
         * ============================================================
         * 1. COLLECT UNIQUE PRODUCT IDS
         * ============================================================
         */
        List<Integer> productIds = request.getItems().stream().map(FmCurrentOnlinePriceItemRequest::getProductId).distinct().toList();

        /*
         * ============================================================
         * 2. ONE DB QUERY
         *
         * Returns:
         * [0] product_id
         * [1] product_variant_id
         * [2] online_price
         * [3] product_name
         * [4] image_link
         * ============================================================
         */
        List<Object[]> pricingRows = pricingRepo.findCurrentOnlinePrices(request.getOutletId(), productIds);

        /*
         * ============================================================
         * 3. BUILD MAP
         * ============================================================
         */
        Map<String, FmCurrentOnlinePriceResponse> priceMap = new HashMap<>();

        for (Object[] row : pricingRows) {

            Integer productId = ((Number) row[0]).intValue();

            Integer variantOptionId = row[1] != null ? ((Number) row[1]).intValue() : null;

            BigDecimal onlinePrice = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            String productName = row[3] != null ? row[3].toString() : null;

            String productImage = row[4] != null ? row[4].toString() : null;

            String key = buildPriceKey(productId, variantOptionId);

            FmCurrentOnlinePriceResponse response = new FmCurrentOnlinePriceResponse();

            response.setProductId(productId);
            response.setVariantOptionId(variantOptionId);
            response.setProductName(productName);
            response.setProductImage(productImage);
            response.setOnlinePrice(onlinePrice);
            response.setAvailable(true);

            priceMap.put(key, response);
        }

        /*
         * ============================================================
         * 4. BUILD RESPONSE IN REQUEST ORDER
         * ============================================================
         */
        List<FmCurrentOnlinePriceResponse> response = new ArrayList<>();

        for (FmCurrentOnlinePriceItemRequest item : request.getItems()) {

            Integer productId = item.getProductId();

            Integer variantOptionId = item.getVariantOptionId();

            String key = buildPriceKey(productId, variantOptionId);

            FmCurrentOnlinePriceResponse currentPrice = priceMap.get(key);

            /*
             * ========================================================
             * PRICE NOT FOUND
             * ========================================================
             */
            if (currentPrice == null) {

                FmCurrentOnlinePriceResponse unavailable = new FmCurrentOnlinePriceResponse();

                unavailable.setProductId(productId);
                unavailable.setVariantOptionId(variantOptionId);
                unavailable.setProductName(null);
                unavailable.setProductImage(null);
                unavailable.setOnlinePrice(BigDecimal.ZERO);
                unavailable.setAvailable(false);

                response.add(unavailable);

                continue;
            }

            response.add(currentPrice);
        }

        log.info("[CURRENT-ONLINE-PRICE] SUCCESS | outletId={} | requested={} | returned={}", request.getOutletId(), request.getItems().size(), response.size());

        return response;
    }

    private String buildPriceKey(Integer productId, Integer variantOptionId) {

        return productId + "_" + (variantOptionId == null ? "NULL" : variantOptionId);
    }


    // ============================================================
    // UPDATE EXISTING ONLINE PRICING ENTITY
    // ============================================================

    private void updateEntity(FmProductOnlinePricing entity, BigDecimal price, LocalDateTime changeTime, boolean isApproved) {

        entity.setOnlinePrice(price.setScale(PRICE_SCALE, RoundingMode.HALF_UP));

        entity.setUpdatedAt(changeTime);

        entity.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        entity.setIsApproved(isApproved);

        if (isApproved) {

            entity.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);
        }
    }


    // ============================================================
    // RESOLVE BASE PRICE
    // ============================================================

    private BigDecimal resolveBasePrice(BigDecimal merchantPrice, BigDecimal existingOnlinePrice) {

        if (merchantPrice == null) {

            throw new PricingException("Merchant price cannot be null");
        }

        merchantPrice = merchantPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

        /*
         * FIRST TIME:
         *
         * onlinePrice null / 0
         *
         * Use merchant price.
         */
        if (existingOnlinePrice == null || existingOnlinePrice.compareTo(BigDecimal.ZERO) <= 0) {

            return merchantPrice;
        }

        /*
         * EXISTING:
         *
         * Use current online price.
         */
        return existingOnlinePrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }


    // ============================================================
    // BULK REQUEST VALIDATION
    // ============================================================

    private void validateRequest(FmBulkPriceUpdateRequestDto dto) {

        if (dto == null) {

            throw new PricingException("Bulk pricing request cannot be null");
        }

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new PricingException("OutletIds cannot be empty");
        }

        for (Integer outletId : dto.getOutletIds()) {

            if (outletId == null || outletId <= 0) {

                throw new PricingException("Invalid outlet id: " + outletId);
            }
        }

        if (dto.getPriceModel() == null || dto.getPriceModel().isBlank()) {

            throw new PricingException("Price model cannot be empty");
        }

        if (dto.getValue() == null || dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {

            throw new PricingException("Price value must be greater than zero");
        }

        if (dto.getOperationType() == null || dto.getOperationType().isBlank()) {

            throw new PricingException("Operation type cannot be empty");
        }

        if (dto.getPriceType() == null || dto.getPriceType().isBlank()) {

            throw new PricingException("Price type cannot be empty");
        }

        if (dto.getLocationType() == null || dto.getLocationType().isBlank()) {

            throw new PricingException("Location type cannot be empty");
        }
    }


    // ============================================================
    // BULK PRICE MODEL VALIDATION
    //
    // IMPORTANT:
    // No Division service call for bulk pricing.
    //
    // Allowed:
    // FLAT
    // PERCENTAGE
    // ============================================================

    private void validateBulkPriceModel(String priceModel) {

        if (priceModel == null || priceModel.isBlank()) {

            throw new PricingException("Price model cannot be empty");
        }

        if (!"FLAT".equalsIgnoreCase(priceModel) && !"PERCENTAGE".equalsIgnoreCase(priceModel)) {

            log.error("Invalid bulk priceModel | priceModel={}", priceModel);

            throw new PricingException("Invalid price model. Allowed values are FLAT or PERCENTAGE");
        }
    }


    // ============================================================
    // VALIDATE HISTORY FIELDS
    //
    // PRICE TYPE:
    // FLAT
    // PERCENTAGE
    //
    // OPERATION:
    // INCREASE
    // DECREASE
    // ============================================================

    private void validateBulkHistoryFields(FmBulkPriceUpdateRequestDto dto) {

        /*
         * ----------------------------------------------------------
         * PRICE TYPE
         * ----------------------------------------------------------
         */

        FmPriceType priceType;

        try {

            priceType = FmPriceType.valueOf(dto.getPriceType().trim().toUpperCase());

        } catch (NullPointerException | IllegalArgumentException ex) {

            throw new PricingException("Invalid price type: " + dto.getPriceType() + ". Allowed values are FLAT or PERCENTAGE");
        }

        /*
         * ----------------------------------------------------------
         * PRICE MODEL AND PRICE TYPE MUST MATCH
         * ----------------------------------------------------------
         */

        if (!dto.getPriceModel().trim().equalsIgnoreCase(priceType.name())) {

            throw new PricingException("Price model and price type must be the same");
        }

        /*
         * ----------------------------------------------------------
         * LOCATION TYPE
         * ----------------------------------------------------------
         */

        if (dto.getLocationType() == null || dto.getLocationType().isBlank()) {

            throw new PricingException("Location type cannot be empty");
        }

        /*
         * ----------------------------------------------------------
         * OPERATION TYPE
         * ----------------------------------------------------------
         */

        FmPriceHistoryOperationType operationType;

        try {

            operationType = FmPriceHistoryOperationType.valueOf(dto.getOperationType().trim().toUpperCase());

        } catch (NullPointerException | IllegalArgumentException ex) {

            throw new PricingException("Invalid operation type: " + dto.getOperationType() + ". Allowed values are INCREASE or DECREASE");
        }

        if (operationType != FmPriceHistoryOperationType.INCREASE && operationType != FmPriceHistoryOperationType.DECREASE) {

            throw new PricingException("Invalid bulk operation type: " + dto.getOperationType() + ". Allowed values are INCREASE or DECREASE");
        }
    }


    // ============================================================
    // CALCULATE FINAL PRICE
    //
    // FLAT:
    //
    // INCREASE -> source + value
    // DECREASE -> source - value
    //
    // PERCENTAGE:
    //
    // INCREASE -> source + source * value / 100
    // DECREASE -> source - source * value / 100
    //
    // Final price can NEVER be negative.
    // ============================================================

    private BigDecimal calculateFinalPrice(BigDecimal sourcePrice, String priceModel, BigDecimal value, String operationType) {

        if (sourcePrice == null) {

            throw new PricingException("Source price cannot be null");
        }

        if (sourcePrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new PricingException("Source price cannot be negative");
        }

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new PricingException("Price value must be greater than zero");
        }

        if (priceModel == null || priceModel.isBlank()) {

            throw new PricingException("Price model cannot be empty");
        }

        if (operationType == null || operationType.isBlank()) {

            throw new PricingException("Operation type cannot be empty");
        }

        BigDecimal source = sourcePrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

        BigDecimal finalPrice;

        if ("FLAT".equalsIgnoreCase(priceModel)) {

            if ("INCREASE".equalsIgnoreCase(operationType)) {

                finalPrice = source.add(value);

            } else if ("DECREASE".equalsIgnoreCase(operationType)) {

                finalPrice = source.subtract(value);

            } else {

                throw new PricingException("Unsupported operation type: " + operationType);
            }

        } else if ("PERCENTAGE".equalsIgnoreCase(priceModel)) {

            BigDecimal percentageAmount = source.multiply(value).divide(BigDecimal.valueOf(100), PRICE_SCALE, RoundingMode.HALF_UP);

            if ("INCREASE".equalsIgnoreCase(operationType)) {

                finalPrice = source.add(percentageAmount);

            } else if ("DECREASE".equalsIgnoreCase(operationType)) {

                finalPrice = source.subtract(percentageAmount);

            } else {

                throw new PricingException("Unsupported operation type: " + operationType);
            }

        } else {

            throw new PricingException("Unsupported price model: " + priceModel);
        }

        /*
         * Never allow negative online price.
         */
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {

            finalPrice = BigDecimal.ZERO;
        }

        return finalPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }


    // ============================================================
    // BUILD BULK PRICE HISTORY
    // ============================================================

    private FmProductPriceChangeHistory buildPriceHistory(FmBulkPriceUpdateRequestDto dto, Integer outletId, Integer productId, Integer productVariantId, BigDecimal oldPrice, BigDecimal newPrice, LocalDateTime changeTime) {

        FmProductPriceChangeHistory history = new FmProductPriceChangeHistory();

        history.setOutletId(outletId);

        history.setProductId(productId);

        /*
         * Bulk flow is product-level only.
         */
        history.setProductVariantId(productVariantId);

        /*
         * Convert request price type to enum.
         */
        history.setPriceType(FmPriceType.valueOf(dto.getPriceType().trim().toUpperCase()));

        /*
         * Bulk update is immediate.
         */
        history.setStartDateTime(null);

        history.setEndDateTime(null);

        history.setOldPrice(oldPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP));

        history.setNewPrice(newPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP));

        history.setOperationType(FmPriceHistoryOperationType.valueOf(dto.getOperationType().trim().toUpperCase()));

        /*
         * Location.
         */
        history.setLocationId(outletId);

        history.setLocationType(dto.getLocationType());

        /*
         * Audit.
         */
        history.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        history.setCreatedAt(changeTime);

        history.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        history.setUpdatedAt(changeTime);

        return history;
    }


    // ============================================================
    // UPSERT PRICE
    // ============================================================

    private void upsertPrice(Integer productId, Integer outletCategoryId, Integer productVariantId, BigDecimal price) {

        if (productId == null) {
            throw new PricingException("ProductId cannot be null");
        }

        if (outletCategoryId == null) {
            throw new PricingException("OutletCategoryId cannot be null");
        }

        if (price == null) {
            throw new PricingException("Price cannot be null");
        }

        price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

        int exists = pricingRepo.existsRow(productId, outletCategoryId, productVariantId);

        if (exists > 0) {

            pricingRepo.updatePrice(productId, outletCategoryId, productVariantId, price, FmAppConstants.DEFAULT_CREATED_BY, FmAppConstants.DEFAULT_CREATED_BY);

            log.info("Updated online price | productId={} | outletCategoryId={} | variantId={} | price={}", productId, outletCategoryId, productVariantId, price);

        } else {

            FmProductOnlinePricing entity = pricingMapper.toEntity(productId, outletCategoryId, productVariantId, price);

            entity.setOnlinePrice(price);

            entity.setCreatedAt(LocalDateTime.now());

            entity.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

            entity.setIsApproved(true);

            entity.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);

            pricingRepo.save(entity);

            log.info("Inserted online price | productId={} | outletCategoryId={} | variantId={} | price={}", productId, outletCategoryId, productVariantId, price);
        }
    }


    // ============================================================
    // GET PRODUCT BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public FmProductDetailResponseDto getProductById(Integer productId) {

        log.info("SERVICE START | GET_PRODUCT | productId={}", productId);

        if (productId == null || productId <= 0) {

            throw new PricingException("Invalid product ID");
        }

        FmProduct product = productRepo.findById(productId).orElseThrow(() -> {

            log.error("PRODUCT_NOT_FOUND | productId={}", productId);

            return new ResourceNotFoundException("Product not found with id: " + productId);
        });

        FmProductDetailResponseDto response = productMapper.toDto(product);

        /*
         * Fetch latest approved product-level online price.
         */
        FmProductOnlinePricing pricing = pricingRepo.findTopByProductIdAndIsApprovedOrderByCreatedAtDesc(productId, true).orElse(null);

        if (pricing != null) {

            response.setOnlinePrice(pricing.getOnlinePrice());

        } else {

            response.setOnlinePrice(product.getMerchantPrice());
        }

        log.info("SERVICE END | GET_PRODUCT_SUCCESS | productId={}", productId);

        return response;
    }


    // ============================================================
    // GET PRODUCT BY PRODUCT ID AND OUTLET ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public FmProductDetailResponseDto getProductByIdAndOutletId(Integer productId, Integer outletId) {

        log.info("SERVICE START | GET_PRODUCT_BY_OUTLET | productId={} | outletId={}", productId, outletId);

        if (productId == null || productId <= 0) {

            throw new PricingException("Invalid product ID");
        }

        if (outletId == null || outletId <= 0) {

            throw new PricingException("Invalid outlet ID");
        }

        FmProductOnlinePricing pricing = pricingRepo.findByProductIdAndOutletId(productId, outletId).orElseThrow(() -> {

            log.warn("PRODUCT_NOT_AVAILABLE_FOR_OUTLET | productId={} | outletId={}", productId, outletId);

            return new PricingException("Product not available for outlet");
        });

        FmProduct product = productRepo.findById(productId).orElseThrow(() -> new PricingException("Product not found"));

        if (!"Y".equalsIgnoreCase(product.getIsActive())) {

            throw new PricingException("Product is inactive");
        }

        FmProductDetailResponseDto dto = new FmProductDetailResponseDto();

        dto.setProductId(product.getProductId());

        dto.setOutletCategoryId(pricing.getOutletCategoryId());

        dto.setProductName(product.getProductName());

        dto.setDescription(product.getDescription());

        dto.setMerchantPrice(product.getMerchantPrice());

        dto.setOnlinePrice(pricing.getOnlinePrice());

        dto.setAvailable(true);

        dto.setIsVeg(product.getIsVeg());

        dto.setHasProductVariants(product.getHasProductVariants());

        dto.setImageLink(product.getImageLink());

        log.info("SERVICE SUCCESS | GET_PRODUCT_BY_OUTLET | productId={} | outletId={} | onlinePrice={}", productId, outletId, pricing.getOnlinePrice());

        return dto;
    }


    // ============================================================
    // COMPOSITE KEY
    // ============================================================

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }

        try {

            return new BigDecimal(value.toString());

        } catch (NumberFormatException ex) {

            throw new PricingException("Invalid numeric price value: " + value);
        }
    }


    // ============================================================
    // CONVERT DATABASE VALUE TO BIG DECIMAL
    // ============================================================

    private record PricingKey(Integer productId, Integer outletCategoryId, Integer variantId) {
    }
}