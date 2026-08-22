package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import com.jippy.foodandmart.entity.FmProductPriceChangeHistory;
import com.jippy.foodandmart.entity.FmProductVariantOption;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
@Transactional
public class FmPricingServiceImpl implements IPricingService {

    private static final Logger log =
            LoggerFactory.getLogger(FmPricingServiceImpl.class);

    private final FmOutletRepository outletRepo;

    private final FmProductRepository productRepo;

    private final FmPricingRepository pricingRepo;

    private final FmPricingMapper pricingMapper;

    private final DivisionFeignClient divisionFeignClient;

    private final FmProductMapper productMapper;

    private final FmProductVariantOptionRepository variantOptionRepo;

    private final FmProductPriceChangeHistoryRepository priceHistoryRepository;

    private final CacheInvalidateServiceImpl cacheInvalidateService;


    // =========================================================
    // GET OUTLETS BASED ON APPROVAL STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FmOutletDto> getOutlets(
            Integer areaId,
            boolean isApproved,
            String search) {

        log.info(
                "SERVICE START: Fetch outlets | areaId={} | isApproved={} | search={}",
                areaId,
                isApproved,
                search
        );

        if (areaId == null) {
            log.error("AreaId is null");
            throw new PricingException("AreaId cannot be null");
        }

        if (search != null && search.isBlank()) {
            search = null;
        }

        List<FmOutlet> outlets =
                isApproved
                        ? outletRepo.findApprovedOutlets(
                        areaId,
                        FmAppConstants.TYPE_OUTLET,
                        search)
                        : outletRepo.findUnapprovedOutlets(
                        areaId,
                        FmAppConstants.TYPE_OUTLET,
                        search);

        if (outlets.isEmpty()) {
            log.warn(
                    "No outlets found for areaId={}",
                    areaId
            );

            throw new PricingException("No outlets found");
        }

        log.info(
                "SERVICE END: Fetched {} outlets",
                outlets.size()
        );

        return outlets.stream()
                .map(o ->
                        new FmOutletDto(
                                o.getOutletId(),
                                o.getOutletName()
                        )
                )
                .toList();
    }


    // =========================================================
    // GET PRODUCTS BASED ON OUTLET IDS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FmProductResponseDto> getProducts(
            List<Integer> outletIds,
            boolean isApproved) {

        log.info(
                "SERVICE START: Fetch products | outletIds={} | isApproved={}",
                outletIds,
                isApproved
        );

        if (outletIds == null || outletIds.isEmpty()) {

            log.error("OutletIds empty");

            throw new PricingException(
                    "OutletIds cannot be empty"
            );
        }

        List<Object[]> rows =
                isApproved
                        ? productRepo.findProducts(outletIds)
                        : productRepo.findProductsWithoutPricing(outletIds);

        if (rows.isEmpty()) {

            log.warn(
                    "No products found for outletIds={}",
                    outletIds
            );

            throw new PricingException(
                    "No products found"
            );
        }

        log.info(
                "SERVICE END: Fetched {} products",
                rows.size()
        );

        return rows.stream()
                .map(pricingMapper::map)
                .toList();
    }


    // =========================================================
    // UPDATE INDIVIDUAL PRODUCT / VARIANT PRICE
    // =========================================================

    @Override
    @Transactional
    public void updatePrices(
            FmPriceUpdateRequestDto dto,
            boolean isApproved) {

        // ---------------------------------------------------------
        // VALIDATE REQUEST BEFORE ACCESSING DTO
        // ---------------------------------------------------------

        if (dto == null) {

            log.error(
                    "SERVICE FAILED: Price update request is null"
            );

            throw new PricingException(
                    "Price update request cannot be null"
            );
        }

        if (dto.getOutletIds() == null ||
                dto.getOutletIds().isEmpty()) {

            throw new PricingException(
                    "OutletIds cannot be empty"
            );
        }

        if (dto.getItems() == null ||
                dto.getItems().isEmpty()) {

            throw new PricingException(
                    "Price update items cannot be empty"
            );
        }

        log.info(
                "SERVICE START: Update prices | outlets={} | items={} | isApproved={}",
                dto.getOutletIds(),
                dto.getItems().size(),
                isApproved
        );

        LocalDateTime now =
                LocalDateTime.now();


        // =========================================================
        // PROCESS EACH OUTLET
        // =========================================================

        for (Integer outletId : dto.getOutletIds()) {

            if (outletId == null) {

                throw new PricingException(
                        "OutletId cannot be null"
                );
            }


            // =====================================================
            // PROCESS EACH PRODUCT / VARIANT
            // =====================================================

            for (FmPriceUpdateRequestDto.Item item : dto.getItems()) {

                if (item == null) {

                    throw new PricingException(
                            "Price update item cannot be null"
                    );
                }

                Integer productId =
                        item.getProductId();

                Integer productVariantId =
                        item.getProductVariantId();

                BigDecimal newPrice =
                        item.getNewPrice();


                // =================================================
                // VALIDATION
                // =================================================

                if (productId == null) {

                    throw new PricingException(
                            "ProductId cannot be null"
                    );
                }

                if (newPrice == null) {

                    throw new PricingException(
                            "New price cannot be null"
                    );
                }

                if (newPrice.compareTo(
                        BigDecimal.ZERO) <= 0) {

                    throw new PricingException(
                            "New price must be greater than zero"
                    );
                }

                log.info(
                        "PROCESS PRICE | outletId={} | productId={} | variantId={} | newPrice={}",
                        outletId,
                        productId,
                        productVariantId,
                        newPrice
                );


                // =================================================
                // 1. FIND OUTLET CATEGORY
                // =================================================

                Integer outletCategoryId =
                        pricingRepo
                                .findOutletCategoryIdByProductAndOutlet(
                                        productId,
                                        outletId
                                )
                                .orElseThrow(() ->
                                        new PricingException(
                                                "Product "
                                                        + productId
                                                        + " is not available for outlet "
                                                        + outletId
                                        )
                                );


                // =================================================
                // 2. GET OLD ONLINE PRICE
                // =================================================

                BigDecimal oldPrice =
                        pricingRepo
                                .findCurrentPriceForScheduledUpdate(
                                        productId,
                                        outletCategoryId,
                                        productVariantId
                                )
                                .orElse(null);

                /*
                 * If no pricing record exists,
                 * this is treated as a new price.
                 */

                if (oldPrice == null) {
                    oldPrice = BigDecimal.ZERO;
                }

                oldPrice =
                        oldPrice.setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


                // =================================================
                // 3. FORMAT NEW PRICE
                // =================================================

                newPrice =
                        newPrice.setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


                log.info(
                        "PRICE COMPARISON | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}",
                        outletId,
                        productId,
                        productVariantId,
                        oldPrice,
                        newPrice
                );


                // =================================================
                // 4. NO CHANGE
                // =================================================

                if (oldPrice.compareTo(newPrice) == 0) {

                    log.info(
                            "PRICE NOT CHANGED | outletId={} | productId={} | variantId={} | price={}",
                            outletId,
                            productId,
                            productVariantId,
                            newPrice
                    );

                    continue;
                }


                // =================================================
                // 5. UPDATE / INSERT ONLINE PRICE
                // =================================================

                upsertPrice(
                        productId,
                        outletCategoryId,
                        productVariantId,
                        newPrice
                );


                // =================================================
                // 6. SAVE PRICE HISTORY
                // =================================================

                savePriceChangeHistory(
                        outletId,
                        productId,
                        productVariantId,
                        oldPrice,
                        newPrice,
                        now
                );


                // =================================================
                // 7. INVALIDATE OUTLET CACHE
                // =================================================

                cacheInvalidateService.invalidateCache(
                        outletId
                );


                log.info(
                        "PRICE UPDATED + HISTORY SAVED + CACHE INVALIDATED | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}",
                        outletId,
                        productId,
                        productVariantId,
                        oldPrice,
                        newPrice
                );
            }
        }


        // =========================================================
        // 8. APPROVE OUTLETS
        // =========================================================

        if (!isApproved) {

            outletRepo.approveOutlets(
                    dto.getOutletIds()
            );

            log.info(
                    "Outlets approved: {}",
                    dto.getOutletIds()
            );
        }


        log.info(
                "SERVICE END: Price update completed successfully"
        );
    }


    // =========================================================
    // SAVE PRICE CHANGE HISTORY
    // =========================================================

    private void savePriceChangeHistory(
            Integer outletId,
            Integer productId,
            Integer productVariantId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            LocalDateTime now) {

        FmProductPriceChangeHistory history =
                new FmProductPriceChangeHistory();


        // ---------------------------------------------------------
        // Outlet
        // ---------------------------------------------------------

        history.setOutletId(
                outletId
        );


        // ---------------------------------------------------------
        // Product
        // ---------------------------------------------------------

        history.setProductId(
                productId
        );


        // ---------------------------------------------------------
        // Variant
        //
        // NULL = main product
        // VALUE = specific variant
        // ---------------------------------------------------------

        history.setProductVariantId(
                productVariantId
        );


        // ---------------------------------------------------------
        // Price Type
        // ---------------------------------------------------------

        history.setPriceType(
                FmPriceType.FLAT
        );


        // ---------------------------------------------------------
        // No scheduled start/end dates
        // ---------------------------------------------------------

        history.setStartDateTime(
                null
        );

        history.setEndDateTime(
                null
        );


        // ---------------------------------------------------------
        // Old Price
        // ---------------------------------------------------------

        history.setOldPrice(
                oldPrice.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );


        // ---------------------------------------------------------
        // New Price
        // ---------------------------------------------------------

        history.setNewPrice(
                newPrice.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );


        // ---------------------------------------------------------
        // Operation
        // ---------------------------------------------------------

        history.setOperationType(
                FmPriceHistoryOperationType.UPDATE
        );


        // ---------------------------------------------------------
        // Location
        // ---------------------------------------------------------

        history.setLocationId(
                outletId
        );

        history.setLocationType(
                "OUTLET"
        );


        // ---------------------------------------------------------
        // Audit
        // ---------------------------------------------------------

        history.setCreatedBy(
                FmAppConstants.DEFAULT_CREATED_BY
        );

        history.setCreatedAt(
                now
        );

        history.setUpdatedBy(
                FmAppConstants.DEFAULT_CREATED_BY
        );

        history.setUpdatedAt(
                now
        );


        // ---------------------------------------------------------
        // Save
        // ---------------------------------------------------------

        priceHistoryRepository.save(
                history
        );


        log.info(
                "PRICE HISTORY SAVED | outletId={} | productId={} | variantId={} | oldPrice={} | newPrice={}",
                outletId,
                productId,
                productVariantId,
                oldPrice,
                newPrice
        );
    }


    // =========================================================
    // BULK UPDATE PRICES
    // =========================================================

    @Override
    @Transactional
    public void bulkUpdatePrices(
            FmBulkPriceUpdateRequestDto dto,
            boolean isApproved) {

        // ---------------------------------------------------------
        // Validate before accessing DTO
        // ---------------------------------------------------------

        if (dto == null) {

            log.error(
                    "BULK FAILED | Request is null"
            );

            throw new PricingException(
                    "Bulk pricing request cannot be null"
            );
        }

        log.info(
                "BULK START | outlets={} | priceModel={} | value={} | isApproved={}",
                dto.getOutletIds(),
                dto.getPriceModel(),
                dto.getValue(),
                isApproved
        );


        // =========================================================
        // VALIDATION
        // =========================================================

        validateRequest(dto);

        validatePriceModel(
                dto.getPriceModel()
        );


        // =========================================================
        // STEP 1:
        // FETCH ALL PRODUCTS FOR ALL OUTLETS IN ONE QUERY
        // =========================================================

        List<Object[]> productRows =
                productRepo.findProductsForBulkPricing(
                        dto.getOutletIds()
                );


        if (productRows.isEmpty()) {

            log.warn(
                    "BULK | No active products found | outlets={}",
                    dto.getOutletIds()
            );


            if (!isApproved) {

                outletRepo.approveOutlets(
                        dto.getOutletIds()
                );
            }

            return;
        }


        log.info(
                "BULK | Products fetched={} | outlets={}",
                productRows.size(),
                dto.getOutletIds()
        );


        // =========================================================
        // STEP 2:
        // EXTRACT PRODUCT IDS
        // =========================================================

        List<Integer> productIds =
                productRows.stream()
                        .map(row ->
                                ((Number) row[0]).intValue()
                        )
                        .distinct()
                        .toList();


        // =========================================================
        // STEP 3:
        // FETCH ALL ACTIVE VARIANTS IN ONE QUERY
        // =========================================================

        List<FmProductVariantOption> variants =
                variantOptionRepo
                        .findActiveVariantsForProducts(
                                productIds
                        );


        Map<Integer, List<FmProductVariantOption>>
                variantsByProduct =
                variants.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FmProductVariantOption::getProductId
                                )
                        );


        log.info(
                "BULK | Variants fetched={} | products={}",
                variants.size(),
                productIds.size()
        );


        // =========================================================
        // STEP 4:
        // EXTRACT OUTLET CATEGORY IDS
        // =========================================================

        List<Integer> outletCategoryIds =
                productRows.stream()
                        .map(row ->
                                ((Number) row[3]).intValue()
                        )
                        .distinct()
                        .toList();


        // =========================================================
        // STEP 5:
        // FETCH EXISTING PRICING ROWS IN ONE QUERY
        // =========================================================

        List<Object[]> existingPricingRows =
                pricingRepo.findExistingBulkPricing(
                        outletCategoryIds
                );


        log.info(
                "BULK | Existing pricing rows={}",
                existingPricingRows.size()
        );


        // =========================================================
        // STEP 6:
        // BUILD EXISTING PRICING ID LIST
        // =========================================================

        List<Integer> existingPricingIds =
                existingPricingRows.stream()
                        .map(row ->
                                ((Number) row[0]).intValue()
                        )
                        .toList();


        // =========================================================
        // STEP 7:
        // LOAD EXISTING ENTITIES IN ONE QUERY
        // =========================================================

        Map<Integer, FmProductOnlinePricing>
                existingPricingById =
                existingPricingIds.isEmpty()
                        ? new HashMap<>()
                        : pricingRepo
                        .findAllById(existingPricingIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        FmProductOnlinePricing
                                        ::getProductOnlinePricingId,
                                        Function.identity()
                                )
                        );


        // =========================================================
        // STEP 8:
        // BUILD PRICING LOOKUP
        //
        // KEY:
        // productId + outletCategoryId + variantId
        //
        // variantId = null
        // means BASE PRODUCT PRICE
        // =========================================================

        Map<PricingKey, FmProductOnlinePricing>
                existingPricingMap =
                new HashMap<>();


        for (Object[] row :
                existingPricingRows) {

            Integer pricingId =
                    ((Number) row[0]).intValue();

            Integer productId =
                    ((Number) row[1]).intValue();

            Integer outletCategoryId =
                    ((Number) row[2]).intValue();

            Integer variantId =
                    row[3] == null
                            ? null
                            : ((Number) row[3]).intValue();


            FmProductOnlinePricing entity =
                    existingPricingById.get(
                            pricingId
                    );


            if (entity != null) {

                existingPricingMap.put(
                        new PricingKey(
                                productId,
                                outletCategoryId,
                                variantId
                        ),
                        entity
                );
            }
        }


        // =========================================================
        // STEP 9:
        // PREPARE CHANGES IN MEMORY
        // =========================================================

        List<FmProductOnlinePricing>
                entitiesToSave =
                new ArrayList<>();


        int productCount = 0;

        int variantCount = 0;

        int insertCount = 0;

        int updateCount = 0;


        // =========================================================
        // PROCESS PRODUCTS
        // =========================================================

        for (Object[] row :
                productRows) {

            Integer productId =
                    ((Number) row[0]).intValue();

            BigDecimal merchantPrice =
                    (BigDecimal) row[2];

            Integer outletCategoryId =
                    ((Number) row[3]).intValue();


            if (merchantPrice == null) {

                throw new PricingException(
                        "Merchant price cannot be null for product "
                                + productId
                );
            }


            // =====================================================
            // BASE PRODUCT PRICE
            // =====================================================

            BigDecimal productOnlinePrice =
                    calculateFinalPrice(
                            merchantPrice,
                            dto.getPriceModel(),
                            dto.getValue()
                    );


            PricingKey productKey =
                    new PricingKey(
                            productId,
                            outletCategoryId,
                            null
                    );


            FmProductOnlinePricing
                    existingProductPricing =
                    existingPricingMap.get(
                            productKey
                    );


            if (existingProductPricing != null) {

                updateEntity(
                        existingProductPricing,
                        productOnlinePrice
                );

                entitiesToSave.add(
                        existingProductPricing
                );

                updateCount++;

            } else {

                FmProductOnlinePricing newEntity =
                        pricingMapper.toEntity(
                                productId,
                                outletCategoryId,
                                null,
                                productOnlinePrice
                        );

                entitiesToSave.add(
                        newEntity
                );

                insertCount++;
            }


            productCount++;


            // =====================================================
            // VARIANT PRICES
            // =====================================================

            List<FmProductVariantOption>
                    productVariants =
                    variantsByProduct.getOrDefault(
                            productId,
                            Collections.emptyList()
                    );


            for (FmProductVariantOption variant :
                    productVariants) {

                BigDecimal variantMerchantPrice =
                        variant.getVariantPrice();


                if (variantMerchantPrice == null) {

                    throw new PricingException(
                            "Variant price cannot be null | productId="
                                    + productId
                                    + " | variantId="
                                    + variant.getProductVariantOptionsId()
                    );
                }


                BigDecimal variantOnlinePrice =
                        calculateFinalPrice(
                                variantMerchantPrice,
                                dto.getPriceModel(),
                                dto.getValue()
                        );


                Integer variantId =
                        variant.getProductVariantOptionsId();


                PricingKey variantKey =
                        new PricingKey(
                                productId,
                                outletCategoryId,
                                variantId
                        );


                FmProductOnlinePricing
                        existingVariantPricing =
                        existingPricingMap.get(
                                variantKey
                        );


                if (existingVariantPricing != null) {

                    updateEntity(
                            existingVariantPricing,
                            variantOnlinePrice
                    );

                    entitiesToSave.add(
                            existingVariantPricing
                    );

                    updateCount++;

                } else {

                    FmProductOnlinePricing newEntity =
                            pricingMapper.toEntity(
                                    productId,
                                    outletCategoryId,
                                    variantId,
                                    variantOnlinePrice
                            );

                    entitiesToSave.add(
                            newEntity
                    );

                    insertCount++;
                }


                variantCount++;
            }


            // =====================================================
            // INVALIDATE OUTLET CACHE
            // =====================================================

            Integer outletId =
                    cacheInvalidateService
                            .getOutletIdForProduct(
                                    productId
                            );


            if (outletId != null) {

                cacheInvalidateService.invalidateCache(
                        outletId
                );

                log.info(
                        "BULK CACHE INVALIDATED | outletId={} | productId={}",
                        outletId,
                        productId
                );
            }
        }


        // =========================================================
        // STEP 10:
        // ONE SAVE ALL
        // =========================================================

        if (!entitiesToSave.isEmpty()) {

            pricingRepo.saveAll(
                    entitiesToSave
            );

            log.info(
                    "BULK V2 | Pricing persisted | total={} | inserts={} | updates={}",
                    entitiesToSave.size(),
                    insertCount,
                    updateCount
            );
        }


        // =========================================================
        // STEP 11:
        // APPROVE OUTLETS FOR UNAPPROVED FLOW
        // =========================================================

        if (!isApproved) {

            outletRepo.approveOutlets(
                    dto.getOutletIds()
            );

            log.info(
                    "BULK V2 | Outlets approved | outlets={}",
                    dto.getOutletIds()
            );
        }


        log.info(
                "BULK END | products={} | variants={} | inserts={} | updates={}",
                productCount,
                variantCount,
                insertCount,
                updateCount
        );
    }


    // =========================================================
    // UPDATE EXISTING PRICING ENTITY
    // =========================================================

    private void updateEntity(
            FmProductOnlinePricing entity,
            BigDecimal price) {

        entity.setOnlinePrice(
                price
        );

        entity.setUpdatedAt(
                LocalDateTime.now()
        );

        entity.setUpdatedBy(
                FmAppConstants.DEFAULT_CREATED_BY
        );

        entity.setIsApproved(
                true
        );

        entity.setApprovedBy(
                FmAppConstants.DEFAULT_CREATED_BY
        );
    }


    // =========================================================
    // VALIDATE BULK REQUEST
    // =========================================================

    private void validateRequest(
            FmBulkPriceUpdateRequestDto dto) {

        if (dto == null) {

            throw new PricingException(
                    "Bulk pricing request cannot be null"
            );
        }


        if (dto.getOutletIds() == null ||
                dto.getOutletIds().isEmpty()) {

            throw new PricingException(
                    "OutletIds cannot be empty"
            );
        }


        if (dto.getPriceModel() == null ||
                dto.getPriceModel().isBlank()) {

            throw new PricingException(
                    "Price model cannot be empty"
            );
        }


        if (dto.getValue() == null ||
                dto.getValue().compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new PricingException(
                    "Price value must be greater than zero"
            );
        }
    }


    // =========================================================
    // VALIDATE PRICE MODEL
    // =========================================================

    private void validatePriceModel(
            String priceModel) {

        log.info(
                "Validating priceModel via Division service | priceModel={}",
                priceModel
        );


        ResponseEntity<List<FmDivPriceModelDto>>
                response =
                divisionFeignClient.getPriceModels();


        if (response == null ||
                response.getBody() == null) {

            log.error(
                    "Division service returned empty pricing models"
            );

            throw new PricingException(
                    "Unable to fetch pricing models"
            );
        }


        boolean valid =
                response.getBody()
                        .stream()
                        .anyMatch(
                                model ->
                                        model.getPriceModelName()
                                                .equalsIgnoreCase(
                                                        priceModel
                                                )
                        );


        if (!valid) {

            log.error(
                    "Invalid priceModel received | priceModel={}",
                    priceModel
            );

            throw new PricingException(
                    "Invalid pricing type"
            );
        }
    }


    // =========================================================
    // CALCULATE FINAL PRICE
    // =========================================================

    private BigDecimal calculateFinalPrice(
            BigDecimal sourcePrice,
            String priceModel,
            BigDecimal value) {

        if (sourcePrice == null) {

            throw new PricingException(
                    "Source price cannot be null"
            );
        }


        if (value == null ||
                value.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new PricingException(
                    "Price value must be greater than zero"
            );
        }


        BigDecimal finalPrice;


        // =====================================================
        // FLAT
        // =====================================================

        if ("FLAT".equalsIgnoreCase(
                priceModel)) {

            finalPrice =
                    sourcePrice.add(
                            value
                    );


            // =====================================================
            // PERCENTAGE
            // =====================================================

        } else if ("PERCENTAGE".equalsIgnoreCase(
                priceModel)) {

            BigDecimal percentageAmount =
                    sourcePrice
                            .multiply(value)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            finalPrice =
                    sourcePrice.add(
                            percentageAmount
                    );


        } else {

            throw new PricingException(
                    "Unsupported price model: "
                            + priceModel
            );
        }


        return finalPrice.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    // =========================================================
    // INSERT / UPDATE ONLINE PRICE
    // =========================================================

    private void upsertPrice(
            Integer productId,
            Integer outletCategoryId,
            Integer productVariantId,
            BigDecimal price) {

        int exists =
                pricingRepo.existsRow(
                        productId,
                        outletCategoryId,
                        productVariantId
                );


        if (exists > 0) {

            pricingRepo.updatePrice(
                    productId,
                    outletCategoryId,
                    productVariantId,
                    price,
                    FmAppConstants.DEFAULT_CREATED_BY,
                    FmAppConstants.DEFAULT_CREATED_BY
            );


            log.info(
                    "Updated online price | productId={} | outletCategoryId={} | variantId={} | price={}",
                    productId,
                    outletCategoryId,
                    productVariantId,
                    price
            );


        } else {

            FmProductOnlinePricing entity =
                    pricingMapper.toEntity(
                            productId,
                            outletCategoryId,
                            productVariantId,
                            price
                    );


            pricingRepo.save(
                    entity
            );


            log.info(
                    "Inserted online price | productId={} | outletCategoryId={} | variantId={} | price={}",
                    productId,
                    outletCategoryId,
                    productVariantId,
                    price
            );
        }
    }


    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @Override
    public FmProductDetailResponseDto getProductById(
            Integer productId) {

        log.info(
                "SERVICE_START | GET_PRODUCT | productId={}",
                productId
        );


        // =====================================================
        // VALIDATE INPUT
        // =====================================================

        if (productId == null ||
                productId <= 0) {

            log.error(
                    "VALIDATION_FAILED | INVALID_PRODUCT_ID | productId={}",
                    productId
            );

            throw new PricingException(
                    "Invalid product ID"
            );
        }


        // =====================================================
        // FETCH PRODUCT
        // =====================================================

        FmProduct product =
                productRepo
                        .findById(productId)
                        .orElseThrow(() -> {

                            log.error(
                                    "PRODUCT_NOT_FOUND | productId={}",
                                    productId
                            );

                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });


        log.info(
                "PRODUCT_FETCHED | productId={} | productName={}",
                product.getProductId(),
                product.getProductName()
        );


        // =====================================================
        // MAP PRODUCT TO DTO
        // =====================================================

        FmProductDetailResponseDto response =
                productMapper.toDto(
                        product
                );


        // =====================================================
        // FETCH LATEST APPROVED ONLINE PRICE
        // =====================================================

        FmProductOnlinePricing pricing =
                pricingRepo
                        .findTopByProductIdAndIsApprovedOrderByCreatedAtDesc(
                                productId,
                                true
                        )
                        .orElse(null);


        // =====================================================
        // SET ONLINE PRICE
        // =====================================================

        if (pricing != null) {

            response.setOnlinePrice(
                    pricing.getOnlinePrice()
            );


            log.info(
                    "ONLINE_PRICE_FETCHED | productId={} | onlinePrice={}",
                    productId,
                    pricing.getOnlinePrice()
            );


        } else {

            // =================================================
            // FALLBACK TO MERCHANT PRICE
            // =================================================

            response.setOnlinePrice(
                    product.getMerchantPrice()
            );


            log.warn(
                    "ONLINE_PRICE_NOT_FOUND | productId={} | fallbackMerchantPrice={}",
                    productId,
                    product.getMerchantPrice()
            );
        }


        log.info(
                "SERVICE_END | GET_PRODUCT_SUCCESS | productId={}",
                productId
        );


        return response;
    }


    // =========================================================
    // GET PRODUCT BY PRODUCT ID + OUTLET ID
    // =========================================================

    @Override
    public FmProductDetailResponseDto getProductByIdAndOutletId(
            Integer productId,
            Integer outletId) {

        log.info(
                "SERVICE_START | GET_PRODUCT_BY_OUTLET | productId={} | outletId={}",
                productId,
                outletId
        );


        // =====================================================
        // FIND PRODUCT PRICING FOR OUTLET
        // =====================================================

        FmProductOnlinePricing pricing =
                pricingRepo
                        .findByProductIdAndOutletId(
                                productId,
                                outletId
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "PRODUCT_NOT_AVAILABLE_FOR_OUTLET | productId={} | outletId={}",
                                    productId,
                                    outletId
                            );

                            return new PricingException(
                                    "Product not available for outlet"
                            );
                        });


        log.debug(
                "PRICING_FOUND | productId={} | outletCategoryId={} | onlinePrice={}",
                productId,
                pricing.getOutletCategoryId(),
                pricing.getOnlinePrice()
        );


        // =====================================================
        // FETCH PRODUCT
        // =====================================================

        FmProduct product =
                productRepo
                        .findById(productId)
                        .orElseThrow(() -> {

                            log.error(
                                    "PRODUCT_NOT_FOUND | productId={}",
                                    productId
                            );

                            return new PricingException(
                                    "Product not found"
                            );
                        });


        // =====================================================
        // CHECK ACTIVE STATUS
        // =====================================================

        if (!"Y".equalsIgnoreCase(
                product.getIsActive())) {

            log.warn(
                    "PRODUCT_INACTIVE | productId={} | productName={}",
                    productId,
                    product.getProductName()
            );

            throw new PricingException(
                    "Product is inactive"
            );
        }


        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        FmProductDetailResponseDto dto =
                new FmProductDetailResponseDto();


        dto.setProductId(
                product.getProductId()
        );

        dto.setOutletCategoryId(
                pricing.getOutletCategoryId()
        );

        dto.setProductName(
                product.getProductName()
        );

        dto.setDescription(
                product.getDescription()
        );

        dto.setMerchantPrice(
                product.getMerchantPrice()
        );

        dto.setOnlinePrice(
                pricing.getOnlinePrice()
        );

        dto.setAvailable(
                true
        );

        dto.setIsVeg(
                product.getIsVeg()
        );

        dto.setHasProductVariants(
                product.getHasProductVariants()
        );

        dto.setImageLink(
                product.getImageLink()
        );


        log.info(
                "SERVICE_SUCCESS | GET_PRODUCT_BY_OUTLET | productId={} | outletId={} | onlinePrice={}",
                productId,
                outletId,
                pricing.getOnlinePrice()
        );


        return dto;
    }


    // =========================================================
    // COMPOSITE KEY FOR BULK PRICING
    // =========================================================

    private record PricingKey(
            Integer productId,
            Integer outletCategoryId,
            Integer variantId) {
    }
}