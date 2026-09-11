package com.jippy.foodandmart.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.foodandmart.dto.FmMapToProductRequest;
import com.jippy.foodandmart.dto.FmMapToProductResult;
import com.jippy.foodandmart.dto.FmMasterProductMappingResultDTO;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.entity.FmMasterProduct;
import com.jippy.foodandmart.entity.FmOutletCategory;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductAvailableTiming;
import com.jippy.foodandmart.entity.FmProductVariant;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.repository.FmDaysOfWeekRepository;
import com.jippy.foodandmart.repository.FmMasterProductRepository;
import com.jippy.foodandmart.repository.FmOutletCategoryRepository;
import com.jippy.foodandmart.repository.FmProductAvailableTimingRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.FmProductVariantRepository;
import com.jippy.foodandmart.service.IFmProductMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmProductMappingServiceImpl implements IFmProductMappingService {

    private final FmProductRepository productRepository;
    private final FmProductVariantRepository productVariantRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmCategoryRepository categoryRepository;
    private final FmMasterProductRepository masterProductRepository;
    private final FmProductAvailableTimingRepository productAvailableTimingRepository;
    private final FmDaysOfWeekRepository daysOfWeekRepository;
    private final ObjectMapper objectMapper;


    // ============================================================
    // MAP SELECTED MASTER PRODUCTS -> OUTLET PRODUCTS
    // ============================================================

    @Override
    public FmMapToProductResult mapToProducts(
            FmMapToProductRequest req) {

        if (req == null) {
            throw new IllegalArgumentException(
                    "Mapping request cannot be null."
            );
        }

        if (req.getProducts() == null ||
                req.getProducts().isEmpty()) {

            throw new IllegalArgumentException(
                    "No products provided."
            );
        }

        /*
         * ==========================================================
         * OUTLET CATEGORY IS REQUIRED
         * ==========================================================
         *
         * The selected outlet category is the destination
         * for every selected master product.
         */
        if (req.getOutletCategoryId() == null ||
                req.getOutletCategoryId() <= 0) {

            throw new IllegalArgumentException(
                    "Outlet category ID is required."
            );
        }

        /*
         * ==========================================================
         * LOAD OUTLET CATEGORY
         * ==========================================================
         */
        FmOutletCategory outletCategory =
                outletCategoryRepository
                        .findById(req.getOutletCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OutletCategory ID "
                                                + req.getOutletCategoryId()
                                                + " not found."
                                )
                        );

        /*
         * ==========================================================
         * CHECK OUTLET ID
         * ==========================================================
         */
        if (req.getOutletId() != null &&
                !req.getOutletId().equals(
                        outletCategory.getOutletId()
                )) {

            throw new IllegalArgumentException(
                    "OutletCategory ID "
                            + req.getOutletCategoryId()
                            + " does not belong to outlet "
                            + req.getOutletId()
            );
        }

        /*
         * Category attached to the selected outlet category.
         */
        Integer outletCategoryCategoryId =
                outletCategory.getCategoryId();

        if (outletCategoryCategoryId == null ||
                outletCategoryCategoryId <= 0) {

            throw new IllegalArgumentException(
                    "Selected outlet category does not have a valid category."
            );
        }

        /*
         * ==========================================================
         * RESULT COLLECTIONS
         * ==========================================================
         */
        List<String> savedNames =
                new ArrayList<>();

        List<String> skippedNames =
                new ArrayList<>();


        // ============================================================
        // PROCESS EACH MASTER PRODUCT
        // ============================================================

        for (FmMapToProductRequest.ProductEntry entry
                : req.getProducts()) {

            if (entry == null) {

                skippedNames.add(
                        "(null)"
                );

                continue;
            }


            /*
             * ======================================================
             * MASTER PRODUCT ID
             * ======================================================
             */
            if (entry.getMasterProductId() == null ||
                    entry.getMasterProductId() <= 0) {

                String requestName =
                        entry.getProductName() == null
                                ? "(unknown)"
                                : entry.getProductName().trim();

                skippedNames.add(
                        requestName
                                + " (invalid master product ID)"
                );

                continue;
            }


            /*
             * ======================================================
             * FETCH MASTER PRODUCT
             * ======================================================
             */
            FmMasterProduct masterProduct =
                    masterProductRepository
                            .findById(
                                    entry.getMasterProductId()
                            )
                            .orElse(null);

            if (masterProduct == null) {

                skippedNames.add(
                        "Master Product ID "
                                + entry.getMasterProductId()
                                + " (not found)"
                );

                continue;
            }


            /*
             * ======================================================
             * MASTER PRODUCT NAME
             * ======================================================
             */
            String name =
                    masterProduct.getMasterProductName() == null
                            ? ""
                            : masterProduct
                            .getMasterProductName()
                            .trim();

            if (name.isBlank()) {

                skippedNames.add(
                        "(blank master product name)"
                );

                continue;
            }


            /*
             * ======================================================
             * MASTER CATEGORY
             * ======================================================
             */
            Integer masterCategoryId =
                    masterProduct.getCategoryId();

            String masterCategoryName =
                    masterProduct.getCategoryName() == null
                            ? ""
                            : masterProduct
                            .getCategoryName()
                            .trim();

            if (masterCategoryId == null ||
                    masterCategoryId <= 0) {

                skippedNames.add(
                        name
                                + " (master product has no category)"
                );

                continue;
            }


            /*
             * ======================================================
             * VALIDATE CATEGORY MATCH
             * ======================================================
             *
             * The selected outlet category must belong to
             * the same global category as the master product.
             */
            if (!masterCategoryId.equals(
                    outletCategoryCategoryId
            )) {

                skippedNames.add(
                        name
                                + " (category mismatch)"
                );

                log.warn(
                        "[MAP] Category mismatch | " +
                                "masterProductId={} | " +
                                "product={} | " +
                                "masterCategoryId={} | " +
                                "masterCategoryName={} | " +
                                "outletCategoryId={} | " +
                                "outletCategoryCategoryId={}",
                        masterProduct.getMasterProductId(),
                        name,
                        masterCategoryId,
                        masterCategoryName,
                        req.getOutletCategoryId(),
                        outletCategoryCategoryId
                );

                continue;
            }


            /*
             * ======================================================
             * DUPLICATE PRODUCT CHECK
             * ======================================================
             *
             * Current products table does not have master_product_id.
             *
             * Therefore duplicate detection is:
             *
             * outlet_category_id + product_name
             *
             * Same product + same outlet category = invalid duplicate.
             */
            boolean alreadyExists =
                    productRepository
                            .existsByOutletCategoryIdAndProductNameIgnoreCase(
                                    req.getOutletCategoryId(),
                                    name
                            );

            if (alreadyExists) {

                skippedNames.add(
                        name + " (Already Exists)"
                );

                log.info(
                        "[MAP] Duplicate product skipped | " +
                                "product={} | " +
                                "outletCategoryId={}",
                        name,
                        req.getOutletCategoryId()
                );

                continue;
            }


            /*
             * ======================================================
             * MASTER DESCRIPTION
             * ======================================================
             */
            String description =
                    masterProduct.getDescription() == null
                            ? ""
                            : masterProduct
                            .getDescription()
                            .trim();

            /*
             * products.description is NOT NULL.
             */
            if (description.length() > 500) {

                description =
                        description.substring(
                                0,
                                500
                        );
            }


            /*
             * ======================================================
             * MASTER PRODUCT TYPE
             * ======================================================
             */
            String productType =
                    masterProduct.getProductType() == null
                            ? ""
                            : masterProduct
                            .getProductType()
                            .trim();

            if (productType.isBlank()) {

                skippedNames.add(
                        name
                                + " (product type missing)"
                );

                continue;
            }

            if (productType.length() > 20) {

                productType =
                        productType.substring(
                                0,
                                20
                        );
            }


            /*
             * ======================================================
             * MASTER VEG STATUS
             * ======================================================
             */
            Boolean isVeg =
                    masterProduct.getIsVeg();

            if (isVeg == null) {

                skippedNames.add(
                        name
                                + " (isVeg missing)"
                );

                continue;
            }


            /*
             * ======================================================
             * MASTER IMAGE
             * ======================================================
             */
            String imageLink =
                    masterProduct.getPhoto();

            if (imageLink == null ||
                    imageLink.isBlank()) {

                throw new IllegalArgumentException(
                        "Product '"
                                + name
                                + "' cannot be added: no image found "
                                + "in master product."
                );
            }


            /*
             * ======================================================
             * HAS VARIANTS
             * ======================================================
             */
            boolean hasVariants =
                    Boolean.TRUE.equals(
                            entry.getHasProductVariants()
                    )
                            && entry.getVariants() != null
                            && !entry.getVariants().isEmpty();


            /*
             * ======================================================
             * MERCHANT PRICE
             * ======================================================
             *
             * IMPORTANT:
             *
             * Price comes from:
             *
             * CSV
             *   ↓
             * Compare
             *   ↓
             * Add To Outlet
             *   ↓
             * entry.merchantPrice
             *
             * We do NOT use the old static price mapper.
             */
            BigDecimal merchantPrice =
                    entry.getMerchantPrice();

            if (merchantPrice == null) {

                merchantPrice =
                        BigDecimal.ZERO;
            }

            if (merchantPrice.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                throw new IllegalArgumentException(
                        "Merchant price cannot be negative "
                                + "for product: "
                                + name
                );
            }


            /*
             * ======================================================
             * CREATE OUTLET PRODUCT
             * ======================================================
             */
            FmProduct product =
                    new FmProduct();


            /*
             * Outlet category
             */
            product.setOutletCategoryId(
                    req.getOutletCategoryId()
            );


            /*
             * Product name FROM MASTER
             */
            product.setProductName(
                    name
            );


            /*
             * Description FROM MASTER
             */
            product.setDescription(
                    description
            );


            /*
             * Merchant price FROM CSV/UI
             */
            product.setMerchantPrice(
                    merchantPrice
            );


            /*
             * Veg status FROM MASTER
             */
            product.setIsVeg(
                    isVeg
            );


            /*
             * Variants
             */
            product.setHasProductVariants(
                    hasVariants
            );


            /*
             * Image FROM MASTER
             */
            product.setImageLink(
                    imageLink
            );


            /*
             * Product type FROM MASTER
             */
            product.setProductType(
                    productType
            );


            /*
             * Required DB defaults.
             */
            product.setIsImageDescUpdated(
                    Boolean.FALSE
            );

            product.setIsActive(
                    "Y"
            );

            product.setIsToggle(
                    Boolean.TRUE
            );


            /*
             * ======================================================
             * SAVE PRODUCT
             * ======================================================
             */
            log.info(
                    "[MAP] Saving outlet product | " +
                            "masterProductId={} | " +
                            "productName={} | " +
                            "categoryId={} | " +
                            "categoryName={} | " +
                            "outletCategoryId={} | " +
                            "merchantPrice={} | " +
                            "productType={} | " +
                            "isVeg={}",
                    masterProduct.getMasterProductId(),
                    name,
                    masterCategoryId,
                    masterCategoryName,
                    req.getOutletCategoryId(),
                    merchantPrice,
                    productType,
                    isVeg
            );


            FmProduct saved =
                    productRepository.save(
                            product
                    );


            /*
             * ======================================================
             * SAVE TIMINGS
             * ======================================================
             */
            saveTimings(
                    saved.getProductId(),
                    entry
            );


            /*
             * ======================================================
             * SAVE VARIANTS
             * ======================================================
             */
            if (hasVariants) {

                for (FmMapToProductRequest.VariantEntry ve
                        : entry.getVariants()) {

                    if (ve == null ||
                            ve.getVariantName() == null ||
                            ve.getVariantName().isBlank()) {

                        continue;
                    }

                    FmProductVariant variant =
                            new FmProductVariant();

                    variant.setProductId(
                            saved.getProductId()
                    );

                    variant.setVariantName(
                            ve.getVariantName()
                                    .trim()
                    );

                    variant.setMerchantPrice(
                            ve.getMerchantPrice() == null
                                    ? BigDecimal.ZERO
                                    : ve.getMerchantPrice()
                    );

                    productVariantRepository.save(
                            variant
                    );
                }
            }


            /*
             * ======================================================
             * SUCCESS
             * ======================================================
             */
            savedNames.add(
                    name
            );
        }


        /*
         * ==========================================================
         * LOG RESULT
         * ==========================================================
         */
        log.info(
                "[MAP] Completed | saved={} | skipped={}",
                savedNames.size(),
                skippedNames.size()
        );


        /*
         * ==========================================================
         * RESPONSE
         * ==========================================================
         */
        FmMapToProductResult result =
                new FmMapToProductResult();

        result.setSavedCount(
                savedNames.size()
        );

        result.setSkippedCount(
                skippedNames.size()
        );

        result.setSavedNames(
                savedNames
        );

        result.setSkippedNames(
                skippedNames
        );

        return result;
    }


    // ============================================================
    // MAP ALL MASTER PRODUCTS BY CATEGORY
    // ============================================================

    @Override
    public FmMasterProductMappingResultDTO mapFromMasterByCategory(
            Integer outletCategoryId) {

        if (outletCategoryId == null ||
                outletCategoryId <= 0) {

            throw new IllegalArgumentException(
                    "OutletCategory ID cannot be null or invalid."
            );
        }


        /*
         * ==========================================================
         * LOAD OUTLET CATEGORY
         * ==========================================================
         */
        FmOutletCategory outletCategory =
                outletCategoryRepository
                        .findById(outletCategoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OutletCategory ID "
                                                + outletCategoryId
                                                + " not found."
                                )
                        );


        Integer categoryId =
                outletCategory.getCategoryId();


        /*
         * ==========================================================
         * LOAD CATEGORY
         * ==========================================================
         */
        FmCategory category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Category ID "
                                                + categoryId
                                                + " not found."
                                )
                        );


        /*
         * ==========================================================
         * LOAD MASTER PRODUCTS
         * ==========================================================
         */
        List<FmMasterProduct> masterProducts =
                masterProductRepository
                        .findByCategoryIdOrderByMasterProductIdAsc(
                                categoryId
                        );


        log.info(
                "[MASTER-MAP] outletCategoryId={} " +
                        "categoryId={} categoryName={} " +
                        "masterProducts={}",
                outletCategoryId,
                categoryId,
                category.getCategoryName(),
                masterProducts.size()
        );


        List<String> savedNames =
                new ArrayList<>();

        List<String> skippedNames =
                new ArrayList<>();


        /*
         * ==========================================================
         * PROCESS MASTER PRODUCTS
         * ==========================================================
         */
        for (FmMasterProduct mp :
                masterProducts) {

            if (mp == null) {
                continue;
            }


            String name =
                    mp.getMasterProductName() == null
                            ? ""
                            : mp.getMasterProductName()
                            .trim();


            if (name.isBlank()) {

                skippedNames.add(
                        "(blank master product)"
                );

                continue;
            }


            /*
             * ======================================================
             * ACTIVE CHECK
             * ======================================================
             */
            String isActive =
                    mp.getIsActive();

            if (isActive == null ||
                    !"Y".equalsIgnoreCase(
                            isActive.trim()
                    )) {

                skippedNames.add(
                        name + " (inactive)"
                );

                continue;
            }


            /*
             * ======================================================
             * DUPLICATE CHECK
             * ======================================================
             */
            if (productRepository
                    .existsByOutletCategoryIdAndProductNameIgnoreCase(
                            outletCategoryId,
                            name
                    )) {

                skippedNames.add(
                        name + " (Already Exists)"
                );

                continue;
            }


            /*
             * ======================================================
             * HAS VARIANTS
             * ======================================================
             */
            boolean hasVariants =
                    mp.getHasOptions() != null
                            && mp.getHasOptions() == 1
                            && mp.getOptions() != null
                            && !mp.getOptions().isBlank();


            /*
             * ======================================================
             * IMAGE
             * ======================================================
             */
            if (mp.getPhoto() == null ||
                    mp.getPhoto().isBlank()) {

                log.warn(
                        "[MASTER-MAP] Skipping product={} " +
                                "masterProductId={} because image is missing",
                        name,
                        mp.getMasterProductId()
                );

                skippedNames.add(
                        name + " (no image in master product)"
                );

                continue;
            }


            /*
             * ======================================================
             * DESCRIPTION
             * ======================================================
             */
            String description =
                    mp.getDescription() == null
                            ? ""
                            : mp.getDescription()
                            .trim();

            if (description.length() > 500) {

                description =
                        description.substring(
                                0,
                                500
                        );
            }


            /*
             * ======================================================
             * PRODUCT TYPE
             * ======================================================
             */
            String productType =
                    mp.getProductType() == null
                            ? ""
                            : mp.getProductType()
                            .trim();

            if (productType.length() > 20) {

                productType =
                        productType.substring(
                                0,
                                20
                        );
            }


            /*
             * ======================================================
             * CREATE PRODUCT
             * ======================================================
             */
            FmProduct product =
                    new FmProduct();


            product.setOutletCategoryId(
                    outletCategoryId
            );


            /*
             * Master name
             */
            product.setProductName(
                    name
            );


            /*
             * Master description
             */
            product.setDescription(
                    description
            );


            /*
             * Default price for category auto-map.
             *
             * This endpoint does not receive CSV price.
             */
            product.setMerchantPrice(
                    BigDecimal.ZERO
            );


            /*
             * Master veg status
             */
            product.setIsVeg(
                    mp.getIsVeg()
            );


            /*
             * Variant status
             */
            product.setHasProductVariants(
                    hasVariants
            );


            /*
             * Master image
             */
            product.setImageLink(
                    mp.getPhoto()
            );


            /*
             * Master product type
             */
            product.setProductType(
                    productType
            );


            /*
             * Required DB defaults.
             */
            product.setIsImageDescUpdated(
                    Boolean.FALSE
            );

            product.setIsActive(
                    "Y"
            );

            product.setIsToggle(
                    Boolean.TRUE
            );


            /*
             * ======================================================
             * SAVE PRODUCT
             * ======================================================
             */
            FmProduct saved =
                    productRepository.save(
                            product
                    );


            /*
             * ======================================================
             * SAVE MASTER OPTIONS AS VARIANTS
             * ======================================================
             */
            if (hasVariants) {

                try {

                    List<Map<String, Object>> optionsList =
                            objectMapper.readValue(
                                    mp.getOptions(),
                                    new TypeReference<
                                            List<Map<String, Object>>
                                            >() {
                                    }
                            );


                    if (optionsList != null) {

                        for (Map<String, Object> opt :
                                optionsList) {

                            if (opt == null) {
                                continue;
                            }


                            Object nameObject =
                                    opt.get("name");

                            if (nameObject == null) {
                                continue;
                            }


                            String variantName =
                                    nameObject
                                            .toString()
                                            .trim();


                            if (variantName.isBlank()) {
                                continue;
                            }


                            BigDecimal price =
                                    BigDecimal.ZERO;


                            Object priceObject =
                                    opt.get("price");


                            if (priceObject != null) {

                                try {

                                    price =
                                            new BigDecimal(
                                                    priceObject
                                                            .toString()
                                            );

                                } catch (
                                        NumberFormatException ignored
                                ) {

                                    log.warn(
                                            "[MASTER-MAP] Invalid variant price={} " +
                                                    "masterProductId={}",
                                            priceObject,
                                            mp.getMasterProductId()
                                    );
                                }
                            }


                            FmProductVariant variant =
                                    new FmProductVariant();


                            variant.setProductId(
                                    saved.getProductId()
                            );


                            variant.setVariantName(
                                    variantName
                            );


                            variant.setMerchantPrice(
                                    price
                            );


                            productVariantRepository.save(
                                    variant
                            );
                        }
                    }

                } catch (
                        JsonProcessingException e
                ) {

                    log.warn(
                            "[MASTER-MAP] Could not parse options " +
                                    "for masterProductId={}: {}",
                            mp.getMasterProductId(),
                            e.getMessage()
                    );
                }
            }


            savedNames.add(
                    name
            );
        }


        /*
         * ==========================================================
         * RESULT
         * ==========================================================
         */
        log.info(
                "[MASTER-MAP] Completed | " +
                        "outletCategoryId={} | " +
                        "saved={} | " +
                        "skipped={}",
                outletCategoryId,
                savedNames.size(),
                skippedNames.size()
        );


        FmMasterProductMappingResultDTO result =
                new FmMasterProductMappingResultDTO();


        result.setOutletCategoryId(
                outletCategoryId
        );


        result.setCategoryId(
                categoryId
        );


        result.setCategoryName(
                category.getCategoryName()
        );


        result.setTotalMasterProducts(
                masterProducts.size()
        );


        result.setSavedCount(
                savedNames.size()
        );


        result.setSkippedCount(
                skippedNames.size()
        );


        result.setSavedProductNames(
                savedNames
        );


        result.setSkippedProductNames(
                skippedNames
        );


        return result;
    }


    // ============================================================
    // SAVE TIMINGS
    // ============================================================

    private void saveTimings(
            Integer productId,
            FmMapToProductRequest.ProductEntry entry) {

        if (productId == null ||
                entry == null) {

            return;
        }


        /*
         * ==========================================================
         * EXPLICIT TIMINGS
         * ==========================================================
         */
        if (entry.getTimings() != null &&
                !entry.getTimings().isEmpty()) {

            for (FmMapToProductRequest.TimingEntry te :
                    entry.getTimings()) {

                if (te == null) {
                    continue;
                }


                LocalTime start =
                        parseTime(
                                te.getStartTime()
                        );


                LocalTime end =
                        parseTime(
                                te.getEndTime()
                        );


                if (start == null ||
                        end == null) {

                    log.warn(
                            "[MAP] Invalid explicit timing | " +
                                    "productId={} | " +
                                    "start={} | " +
                                    "end={}",
                            productId,
                            te.getStartTime(),
                            te.getEndTime()
                    );

                    continue;
                }


                Integer dayId =
                        te.getDayOfWeekId() == null
                                ? 0
                                : te.getDayOfWeekId();


                FmProductAvailableTiming timing =
                        new FmProductAvailableTiming();


                timing.setProductId(
                        productId
                );


                timing.setDayOfWeekId(
                        dayId
                );


                timing.setStartTime(
                        start
                );


                timing.setEndTime(
                        end
                );


                productAvailableTimingRepository.save(
                        timing
                );


                log.info(
                        "[MAP] Saved explicit timing | " +
                                "productId={} | " +
                                "dayId={} | " +
                                "start={} | " +
                                "end={}",
                        productId,
                        dayId,
                        start,
                        end
                );
            }


            return;
        }


        /*
         * ==========================================================
         * CSV TIMING
         * ==========================================================
         */
        String rawTiming =
                entry.getCsvTiming();


        if (rawTiming == null ||
                rawTiming.isBlank()) {

            log.info(
                    "[MAP] No CSV timing for productId={}",
                    productId
            );

            return;
        }


        /*
         * ==========================================================
         * CSV DAY
         * ==========================================================
         */
        String dayName =
                entry.getCsvDayOfWeek();


        Integer dayId =
                resolveDayId(
                        dayName
                );


        /*
         * ==========================================================
         * TIMING SLOTS
         * ==========================================================
         *
         * Supported:
         *
         * 11:00-22:00
         *
         * 11:00-14:00,18:00-22:00
         *
         * 11:00 AM-02:00 PM
         *
         * 11:00 AM - 02:00 PM
         */
        String[] slots =
                rawTiming
                        .trim()
                        .split(",");


        for (String slot :
                slots) {

            if (slot == null ||
                    slot.isBlank()) {

                continue;
            }


            String trimmedSlot =
                    slot.trim();


            String[] parts =
                    trimmedSlot.split(
                            "-",
                            2
                    );


            if (parts.length != 2) {

                log.warn(
                        "[MAP] Could not parse timing slot '{}' | productId={}",
                        trimmedSlot,
                        productId
                );

                continue;
            }


            LocalTime start =
                    parseTime(
                            parts[0].trim()
                    );


            LocalTime end =
                    parseTime(
                            parts[1].trim()
                    );


            if (start == null ||
                    end == null) {

                log.warn(
                        "[MAP] Invalid timing slot '{}' | productId={}",
                        trimmedSlot,
                        productId
                );

                continue;
            }


            FmProductAvailableTiming timing =
                    new FmProductAvailableTiming();


            timing.setProductId(
                    productId
            );


            timing.setDayOfWeekId(
                    dayId
            );


            timing.setStartTime(
                    start
            );


            timing.setEndTime(
                    end
            );


            productAvailableTimingRepository.save(
                    timing
            );


            log.info(
                    "[MAP] Saved CSV timing | " +
                            "productId={} | " +
                            "dayName={} | " +
                            "dayId={} | " +
                            "start={} | " +
                            "end={}",
                    productId,
                    dayName,
                    dayId,
                    start,
                    end
            );
        }
    }


    // ============================================================
    // RESOLVE DAY OF WEEK
    // ============================================================

    private Integer resolveDayId(
            String dayName) {

        /*
         * 0 = all days
         */
        if (dayName == null ||
                dayName.isBlank()) {

            return 0;
        }


        String finalDayName =
                dayName.trim();


        return daysOfWeekRepository
                .findByDayNameIgnoreCase(
                        finalDayName
                )
                .map(d ->
                        d.getDayId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Day of week '"
                                        + finalDayName
                                        + "' not found in days_of_week table."
                        )
                );
    }


    // ============================================================
    // PARSE TIME
    // ============================================================

    private LocalTime parseTime(
            String raw) {

        if (raw == null ||
                raw.isBlank()) {

            return null;
        }


        String value =
                raw.trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        /*
         * ==========================================================
         * HH:mm
         * ==========================================================
         */
        try {

            return LocalTime.parse(
                    value,
                    DateTimeFormatter.ofPattern(
                            "H:mm"
                    )
            );

        } catch (DateTimeParseException ignored) {
            // Try next format.
        }


        /*
         * ==========================================================
         * HH:mm:ss
         * ==========================================================
         */
        try {

            return LocalTime.parse(
                    value,
                    DateTimeFormatter.ofPattern(
                            "H:mm:ss"
                    )
            );

        } catch (DateTimeParseException ignored) {
            // Try next format.
        }


        /*
         * ==========================================================
         * hh:mm AM/PM
         * ==========================================================
         */
        try {

            return LocalTime.parse(
                    value.toUpperCase(),
                    DateTimeFormatter.ofPattern(
                            "h:mm a"
                    )
            );

        } catch (DateTimeParseException ignored) {
            // Continue.
        }


        /*
         * ==========================================================
         * hh:mm:ss AM/PM
         * ==========================================================
         */
        try {

            return LocalTime.parse(
                    value.toUpperCase(),
                    DateTimeFormatter.ofPattern(
                            "h:mm:ss a"
                    )
            );

        } catch (DateTimeParseException ignored) {
            // Continue.
        }


        log.warn(
                "[MAP] Could not parse time '{}'",
                raw
        );


        return null;
    }
}