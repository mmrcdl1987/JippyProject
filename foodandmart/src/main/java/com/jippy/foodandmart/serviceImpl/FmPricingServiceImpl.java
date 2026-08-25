package com.jippy.foodandmart.serviceImpl;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.PricingException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.mapper.FmPricingMapper;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmPricingRepository;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FmPricingServiceImpl implements IPricingService {
    private static final Logger log = LoggerFactory.getLogger(FmPricingServiceImpl.class);

    private final FmOutletRepository outletRepo;
    private final FmProductRepository productRepo;
    private final FmPricingRepository pricingRepo;
    private final FmPricingMapper pricingMapper;
    private final DivisionFeignClient divisionFeignClient;
    private final FmProductMapper productMapper;
    private final FmProductVariantOptionRepository variantOptionRepo;
    private final CacheInvalidateServiceImpl cacheInvalidateService;


    //  GET OUTLETS BASED ON CONDITION IS_APPROVED
    @Override
    @Transactional(readOnly = true)
    public List<FmOutletDto> getOutlets(Integer areaId, boolean isApproved, String search) {

        log.info("SERVICE START: Fetch outlets | areaId={} | isApproved={} | search={}", areaId, isApproved, search);

        if (areaId == null) {
            log.error("AreaId is null");
            throw new PricingException("AreaId cannot be null");
        }

        if (search != null && search.isBlank()) {
            search = null;
        }

        List<FmOutlet> outlets = isApproved ? outletRepo.findApprovedOutlets(areaId, FmAppConstants.TYPE_OUTLET, search) : outletRepo.findUnapprovedOutlets(areaId, FmAppConstants.TYPE_OUTLET, search);

        if (outlets.isEmpty()) {
            log.warn("No outlets found for areaId={}", areaId);
            throw new PricingException("No outlets found");
        }

        log.info("SERVICE END: Fetched {} outlets", outlets.size());

        return outlets.stream().map(o -> new FmOutletDto(o.getOutletId(), o.getOutletName())).toList();
    }

    //  GET PRODUCTS BASED ON OUTLET IDS
    @Override
    @Transactional(readOnly = true)
    public List<FmProductResponseDto> getProducts(List<Integer> outletIds, boolean isApproved) {

        log.info("SERVICE START: Fetch products | outletIds={} | isApproved={}", outletIds, isApproved);

        if (outletIds == null || outletIds.isEmpty()) {
            log.error("OutletIds empty");
            throw new PricingException("OutletIds cannot be empty");
        }

        List<Object[]> rows = isApproved ? productRepo.findProducts(outletIds) : productRepo.findProductsWithoutPricing(outletIds);

        if (rows.isEmpty()) {
            log.warn("No products found for outletIds={}", outletIds);
            throw new PricingException("No products found");
        }

        log.info("SERVICE END: Fetched {} products", rows.size());

        return rows.stream().map(pricingMapper::map).toList();
    }

    //  UPDATE PRICES FROM PRICING TABLE
    @Override
    @Transactional
    public void updatePrices(FmPriceUpdateRequestDto dto, boolean isApproved) {

        log.info("Updating prices | outlets={} | items={} | isApproved={}", dto.getOutletIds(), dto.getItems().size(), isApproved);

        for (Integer outletId : dto.getOutletIds()) {

            for (FmPriceUpdateRequestDto.Item item : dto.getItems()) {

                Integer productId = item.getProductId();
                Integer productVariantId = item.getProductVariantId();

                Integer outletCategoryId = pricingRepo.findOutletCategoryIdByProductAndOutlet(productId, outletId).orElseThrow(() -> new PricingException("Product " + productId + " is not available for outlet " + outletId));

                log.info("Updating price | outletId={} | productId={} | variantId={} | outletCategoryId={} | price={}", outletId, productId, productVariantId, outletCategoryId, item.getNewPrice());

                upsertPrice(productId, outletCategoryId, productVariantId, item.getNewPrice());

                // Invalidate outlet details cache
                cacheInvalidateService.invalidateCache(outletId);

            }
        }

        log.info("Price update completed successfully.");
    }

    @Override
    @Transactional
    public void bulkUpdatePrices(FmBulkPriceUpdateRequestDto dto, boolean isApproved) {

        log.info("BULK  START | outlets={} | priceModel={} | value={} | isApproved={}", dto.getOutletIds(), dto.getPriceModel(), dto.getValue(), isApproved);

        validateRequest(dto);
        validatePriceModel(dto.getPriceModel());

        /*
         * STEP 1: Fetch all products for all outlets in ONE query
         */
        List<Object[]> productRows = productRepo.findProductsForBulkPricing(dto.getOutletIds());

        if (productRows.isEmpty()) {

            log.warn("BULK  | No active products found | outlets={}", dto.getOutletIds());

            if (!isApproved) {
                outletRepo.approveOutlets(dto.getOutletIds());
            }

            return;
        }

        log.info("BULK  | Products fetched={} | outlets={}", productRows.size(), dto.getOutletIds());

        /*
         * STEP 2: Extract product IDs
         */
        List<Integer> productIds = productRows.stream().map(row -> ((Number) row[0]).intValue()).distinct().toList();

        /*
         * STEP 3: Fetch ALL variants in ONE query
         */
        List<FmProductVariantOption> variants = variantOptionRepo.findActiveVariantsForProducts(productIds);

        Map<Integer, List<FmProductVariantOption>> variantsByProduct = variants.stream().collect(Collectors.groupingBy(FmProductVariantOption::getProductId));

        log.info("BULK  | Variants fetched={} | products={}", variants.size(), productIds.size());

        /*
         * STEP 4: Extract outlet category IDs
         */
        List<Integer> outletCategoryIds = productRows.stream().map(row -> ((Number) row[3]).intValue()).distinct().toList();

        /*
         * STEP 5: Fetch existing pricing rows in ONE query
         */
        List<Object[]> existingPricingRows = pricingRepo.findExistingBulkPricing(outletCategoryIds);

        log.info("BULK | Existing pricing rows={}", existingPricingRows.size());

        /*
         * STEP 6: Build existing pricing ID list
         */
        List<Integer> existingPricingIds = existingPricingRows.stream().map(row -> ((Number) row[0]).intValue()).toList();

        /*
         * STEP 7: Load existing entities in ONE query
         *
         * We do not update Object[] directly because we want
         * to Hibernate managed entities with their existing audit data.
         */
        Map<Integer, FmProductOnlinePricing> existingPricingById = existingPricingIds.isEmpty() ? new HashMap<>() : pricingRepo.findAllById(existingPricingIds).stream().collect(Collectors.toMap(FmProductOnlinePricing::getProductOnlinePricingId, Function.identity()));

        /*
         * STEP 8: Build lookup
         *
         * Key:
         *
         * productId + outletCategoryId + variantId
         *
         * variantId = null means BASE PRODUCT PRICE.
         */
        Map<FmPricingServiceImpl.PricingKey, FmProductOnlinePricing> existingPricingMap = new HashMap<>();

        for (Object[] row : existingPricingRows) {

            Integer pricingId = ((Number) row[0]).intValue();

            Integer productId = ((Number) row[1]).intValue();

            Integer outletCategoryId = ((Number) row[2]).intValue();

            Integer variantId = row[3] == null ? null : ((Number) row[3]).intValue();

            FmProductOnlinePricing entity = existingPricingById.get(pricingId);

            if (entity != null) {

                existingPricingMap.put(new FmPricingServiceImpl.PricingKey(productId, outletCategoryId, variantId), entity);
            }
        }

        /*
         * STEP 9: Prepare changes in memory
         */
        List<FmProductOnlinePricing> entitiesToSave = new ArrayList<>();

        int productCount = 0;
        int variantCount = 0;
        int insertCount = 0;
        int updateCount = 0;

        for (Object[] row : productRows) {

            Integer productId = ((Number) row[0]).intValue();

            BigDecimal merchantPrice = (BigDecimal) row[2];

            Integer outletCategoryId = ((Number) row[3]).intValue();

            if (merchantPrice == null) {
                throw new PricingException("Merchant price cannot be null for product " + productId);
            }

            /*
             * BASE PRODUCT PRICE
             */
            BigDecimal productOnlinePrice = calculateFinalPrice(merchantPrice, dto.getPriceModel(), dto.getValue());

            FmPricingServiceImpl.PricingKey productKey = new FmPricingServiceImpl.PricingKey(productId, outletCategoryId, null);

            FmProductOnlinePricing existingProductPricing = existingPricingMap.get(productKey);

            if (existingProductPricing != null) {

                updateEntity(existingProductPricing, productOnlinePrice);

                entitiesToSave.add(existingProductPricing);

                updateCount++;

            } else {

                FmProductOnlinePricing newEntity = pricingMapper.toEntity(productId, outletCategoryId, null, productOnlinePrice);

                entitiesToSave.add(newEntity);

                insertCount++;
            }

            productCount++;

            /*
             * VARIANT PRICES
             */
            List<FmProductVariantOption> productVariants = variantsByProduct.getOrDefault(productId, Collections.emptyList());

            for (FmProductVariantOption variant : productVariants) {

                BigDecimal variantMerchantPrice = variant.getVariantPrice();

                if (variantMerchantPrice == null) {
                    throw new PricingException("Variant price cannot be null | productId=" + productId + " | variantId=" + variant.getProductVariantOptionsId());
                }

                BigDecimal variantOnlinePrice = calculateFinalPrice(variantMerchantPrice, dto.getPriceModel(), dto.getValue());

                Integer variantId = variant.getProductVariantOptionsId();

                FmPricingServiceImpl.PricingKey variantKey = new FmPricingServiceImpl.PricingKey(productId, outletCategoryId, variantId);

                FmProductOnlinePricing existingVariantPricing = existingPricingMap.get(variantKey);

                if (existingVariantPricing != null) {

                    updateEntity(existingVariantPricing, variantOnlinePrice);

                    entitiesToSave.add(existingVariantPricing);

                    updateCount++;

                } else {

                    FmProductOnlinePricing newEntity = pricingMapper.toEntity(productId, outletCategoryId, variantId, variantOnlinePrice);

                    entitiesToSave.add(newEntity);

                    insertCount++;
                }

                variantCount++;
            }

            // Invalidate outlet details cache
            Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
            cacheInvalidateService.invalidateCache(outletId);
        }

        /*
         * STEP 10: ONE saveAll
         */
        if (!entitiesToSave.isEmpty()) {

            pricingRepo.saveAll(entitiesToSave);

            log.info("BULK V2 | Pricing persisted | total={} | inserts={} | updates={}", entitiesToSave.size(), insertCount, updateCount);
        }

        /*
         * STEP 11: Approve outlets for unapproved flow
         */
        if (!isApproved) {

            outletRepo.approveOutlets(dto.getOutletIds());

            log.info("BULK V2 | Outlets approved | outlets={}", dto.getOutletIds());
        }

        log.info("BULK  END | products={} | variants={} | inserts={} | updates={}", productCount, variantCount, insertCount, updateCount);
    }

    /*
     * UPDATE EXISTING ENTITY
     */
    private void updateEntity(FmProductOnlinePricing entity, BigDecimal price) {

        entity.setOnlinePrice(price);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        entity.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);
        entity.setIsApproved(true);
        entity.setApprovedBy(FmAppConstants.DEFAULT_CREATED_BY);
    }

    /*
     * VALIDATION
     */
    private void validateRequest(FmBulkPriceUpdateRequestDto dto) {

        if (dto == null) {
            throw new PricingException("Bulk pricing request cannot be null");
        }

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new PricingException("OutletIds cannot be empty");
        }

        if (dto.getPriceModel() == null || dto.getPriceModel().isBlank()) {

            throw new PricingException("Price model cannot be empty");
        }

        if (dto.getValue() == null || dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {

            throw new PricingException("Price value must be greater than zero");
        }
    }

    /*
     * PRICE MODEL VALIDATION
     */
    private void validatePriceModel(String priceModel) {

        log.info("Validating priceModel via Division service | priceModel={}", priceModel);

        ResponseEntity<List<FmDivPriceModelDto>> response = divisionFeignClient.getPriceModels();

        if (response == null || response.getBody() == null) {

            log.error("Division service returned empty pricing models");

            throw new PricingException("Unable to fetch pricing models");
        }

        boolean valid = response.getBody().stream().anyMatch(model -> model.getPriceModelName().equalsIgnoreCase(priceModel));

        if (!valid) {

            log.error("Invalid priceModel received | priceModel={}", priceModel);

            throw new PricingException("Invalid pricing type");
        }
    }

    /*
     * PRICE CALCULATION
     */
    private BigDecimal calculateFinalPrice(BigDecimal sourcePrice, String priceModel, BigDecimal value) {

        if (sourcePrice == null) {
            throw new PricingException("Source price cannot be null");
        }

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new PricingException("Price value must be greater than zero");
        }

        BigDecimal finalPrice;

        if ("FLAT".equalsIgnoreCase(priceModel)) {

            finalPrice = sourcePrice.add(value);

        } else if ("PERCENTAGE".equalsIgnoreCase(priceModel)) {

            BigDecimal percentageAmount = sourcePrice.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            finalPrice = sourcePrice.add(percentageAmount);

        } else {

            throw new PricingException("Unsupported price model: " + priceModel);
        }

        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    // ================= HELPER =================

    private void upsertPrice(Integer productId, Integer outletCategoryId, Integer productVariantId, BigDecimal price) {

        int exists = pricingRepo.existsRow(productId, outletCategoryId, productVariantId);

        if (exists > 0) {

            pricingRepo.updatePrice(productId, outletCategoryId, productVariantId, price, FmAppConstants.DEFAULT_CREATED_BY, FmAppConstants.DEFAULT_CREATED_BY);

            log.info("Updated online price | productId={} | outletCategoryId={} | variantId={} | price={}", productId, outletCategoryId, productVariantId, price);

        } else {

            FmProductOnlinePricing entity = pricingMapper.toEntity(productId, outletCategoryId, productVariantId, price);

            pricingRepo.save(entity);

            log.info("Inserted online price | productId={} | outletCategoryId={} | variantId={} | price={}", productId, outletCategoryId, productVariantId, price);
        }
    }

    @Override
    public FmProductDetailResponseDto getProductById(Integer productId) {

        log.info("SERVICE_START | GET_PRODUCT | productId={}", productId);

        /*
         * VALIDATE INPUT
         */
        if (productId == null || productId <= 0) {

            log.error("VALIDATION_FAILED | INVALID_PRODUCT_ID | productId={}", productId);

            throw new PricingException("Invalid product ID");
        }

        /*
         * FETCH PRODUCT
         */
        FmProduct product = productRepo.findById(productId).orElseThrow(() -> {

            log.error("PRODUCT_NOT_FOUND | productId={}", productId);

            return new ResourceNotFoundException("Product not found with id: " + productId);
        });

        log.info("PRODUCT_FETCHED | productId={} | productName={}", product.getProductId(), product.getProductName());

        /*
         * MAP PRODUCT TO DTO
         */
        FmProductDetailResponseDto response = productMapper.toDto(product);

        /*
         * FETCH LATEST APPROVED ONLINE PRICE
         */
        FmProductOnlinePricing pricing = pricingRepo.findTopByProductIdAndIsApprovedOrderByCreatedAtDesc(productId, true).orElse(null);

        /*
         * SET ONLINE PRICE
         */
        if (pricing != null) {

            response.setOnlinePrice(pricing.getOnlinePrice());

            log.info("ONLINE_PRICE_FETCHED | productId={} | onlinePrice={}", productId, pricing.getOnlinePrice());

        } else {

            /*
             * FALLBACK TO MERCHANT PRICE
             */
            response.setOnlinePrice(product.getMerchantPrice());

            log.warn("ONLINE_PRICE_NOT_FOUND | productId={} | fallbackMerchantPrice={}", productId, product.getMerchantPrice());
        }

        log.info("SERVICE_END | GET_PRODUCT_SUCCESS | productId={}", productId);

        return response;
    }

    @Override
    public FmProductDetailResponseDto getProductByIdAndOutletId(Integer productId, Integer outletId) {

        log.info("SERVICE_START | GET_PRODUCT_BY_OUTLET | productId={} | outletId={}", productId, outletId);

        FmProductOnlinePricing pricing = pricingRepo.findByProductIdAndOutletId(productId, outletId).orElseThrow(() -> {

            log.warn("PRODUCT_NOT_AVAILABLE_FOR_OUTLET | productId={} | outletId={}", productId, outletId);

            return new PricingException("Product not available for outlet");
        });

        log.debug("PRICING_FOUND | productId={} | outletCategoryId={} | onlinePrice={}", productId, pricing.getOutletCategoryId(), pricing.getOnlinePrice());

        FmProduct product = productRepo.findById(productId).orElseThrow(() -> {

            log.error("PRODUCT_NOT_FOUND | productId={}", productId);

            return new PricingException("Product not found");
        });

        if (!"Y".equalsIgnoreCase(product.getIsActive())) {

            log.warn("PRODUCT_INACTIVE | productId={} | productName={}", productId, product.getProductName());

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

        log.info("SERVICE_SUCCESS | GET_PRODUCT_BY_OUTLET | productId={} | outletId={} | onlinePrice={}", productId, outletId, pricing.getOnlinePrice());

        return dto;
    }

    /*
     * COMPOSITE KEY
     */
    private record PricingKey(Integer productId, Integer outletCategoryId, Integer variantId) {
    }
}

