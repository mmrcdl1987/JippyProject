package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductOnlinePricing;
import com.jippy.foodandmart.exception.PricingException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.mapper.FmPricingMapper;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmPricingRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.service.IPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
    public void updatePrices(FmPriceUpdateRequestDto priceUpdateRequestDto, boolean isApproved) {

        log.info("SERVICE START: Update prices | outlets={} | itemCount={} | isApproved={}", priceUpdateRequestDto.getOutletIds(), priceUpdateRequestDto.getItems().size(), isApproved);

        validateUpdateRequest(priceUpdateRequestDto);

        for (Integer outletId : priceUpdateRequestDto.getOutletIds()) {

            for (FmPriceUpdateRequestDto.Item item : priceUpdateRequestDto.getItems()) {

                log.debug("Processing | outletId={} | productId={}", outletId, item.getProductId());

                FmProduct product = productRepo.findById(item.getProductId()).orElseThrow(() -> {
                    log.error("Invalid productId={}", item.getProductId());
                    return new PricingException("Invalid productId");
                });

                Integer outletCategoryId = productRepo.findOutletCategoryId(item.getProductId());

                if (outletCategoryId == null) {
                    log.error("OutletCategory not found for productId={}", item.getProductId());
                    throw new PricingException("OutletCategory not found");
                }

                BigDecimal basePrice = resolveBasePrice(item.getProductId(), outletCategoryId, product.getMerchantPrice(), isApproved);

                BigDecimal finalPrice = basePrice.add(item.getNewPrice()).setScale(2, RoundingMode.HALF_UP);

                log.debug("Calculated price | productId={} | base={} | final={}", item.getProductId(), basePrice, finalPrice);

                upsertPrice(item.getProductId(), outletCategoryId, finalPrice);
            }
        }

        if (!isApproved) {
            outletRepo.approveOutlets(priceUpdateRequestDto.getOutletIds());
            log.info("Outlets approved: {}", priceUpdateRequestDto.getOutletIds());
        }

        log.info("SERVICE END: Update prices completed");
    }

    // ================= BULK UPDATE =================
    @Override
    public void bulkUpdatePrices(FmBulkPriceUpdateRequestDto bulkPriceUpdateRequestDto, boolean isApproved) {

        log.info("SERVICE START: Bulk update | outlets={} | priceModel={} | value={}", bulkPriceUpdateRequestDto.getOutletIds(), bulkPriceUpdateRequestDto.getPriceModel(), bulkPriceUpdateRequestDto.getValue());

        if (bulkPriceUpdateRequestDto.getOutletIds() == null || bulkPriceUpdateRequestDto.getOutletIds().isEmpty()) {
            log.error("OutletIds empty");
            throw new PricingException("OutletIds cannot be empty");
        }

        String priceModel = bulkPriceUpdateRequestDto.getPriceModel();

        //  FEIGN VALIDATION
        validatePriceModel(priceModel);

        for (Integer outletId : bulkPriceUpdateRequestDto.getOutletIds()) {

            log.debug("Processing outletId={}", outletId);

            List<Object[]> products = isApproved ? productRepo.findProducts(List.of(outletId)) : productRepo.findProductsWithoutPricing(List.of(outletId));

            if (products.isEmpty()) {
                log.warn("No products for outletId={}", outletId);
                continue;
            }

            for (Object[] row : products) {

                Integer productId = ((Number) row[0]).intValue();
                BigDecimal merchantPrice = (BigDecimal) row[2];

                Integer outletCategoryId = productRepo.findOutletCategoryId(productId);

                BigDecimal basePrice = resolveBasePrice(productId, outletCategoryId, merchantPrice, isApproved);

                BigDecimal finalPrice = calculateFinalPrice(basePrice, priceModel, bulkPriceUpdateRequestDto.getValue()).setScale(2, RoundingMode.HALF_UP);

                log.debug("Bulk price | outletId={} | productId={} | base={} | final={}", outletId, productId, basePrice, finalPrice);

                upsertPrice(productId, outletCategoryId, finalPrice);
            }
        }

        if (!isApproved) {
            outletRepo.approveOutlets(bulkPriceUpdateRequestDto.getOutletIds());
            log.info("Outlets approved: {}", bulkPriceUpdateRequestDto.getOutletIds());
        }

        log.info("SERVICE END: Bulk update completed");
    }

    // ================= HELPER =================

    private void validateUpdateRequest(FmPriceUpdateRequestDto priceUpdateRequestDto) {
        if (priceUpdateRequestDto.getOutletIds() == null || priceUpdateRequestDto.getOutletIds().isEmpty()) {
            log.error("OutletIds empty");
            throw new PricingException("OutletIds cannot be empty");
        }
        if (priceUpdateRequestDto.getItems() == null || priceUpdateRequestDto.getItems().isEmpty()) {
            log.error("Items empty");
            throw new PricingException("Items cannot be empty");
        }
    }

    private void validatePriceModel(String priceModel) {

        log.info("Validating priceModel via Feign: {}", priceModel);

        ResponseEntity<List<FmDivPriceModelDto>> response = divisionFeignClient.getPriceModels();

        if (response == null || response.getBody() == null) {
            log.error("Division service returned empty response");
            throw new PricingException("Unable to fetch pricing models");
        }

        List<FmDivPriceModelDto> models = response.getBody();

        boolean valid = models.stream().anyMatch(m -> m.getPriceModelName().equalsIgnoreCase(priceModel));

        if (!valid) {
            log.error("Invalid priceModel received: {}", priceModel);
            throw new PricingException("Invalid pricing type");
        }

        log.info("PriceModel validated successfully: {}", priceModel);
    }

    private void upsertPrice(Integer productId, Integer outletCategoryId, BigDecimal price) {

        int exists = pricingRepo.existsRow(productId, outletCategoryId);

        if (exists > 0) {
            pricingRepo.updatePrice(productId, outletCategoryId, price, FmAppConstants.DEFAULT_CREATED_BY, FmAppConstants.DEFAULT_CREATED_BY);
            log.debug("Updated price | productId={} | outletCategoryId={}", productId, outletCategoryId);

        } else {
            pricingRepo.save(pricingMapper.toEntity(productId, outletCategoryId, price));
            log.debug("Inserted price | productId={} | outletCategoryId={}", productId, outletCategoryId);
        }
    }

    private BigDecimal resolveBasePrice(Integer productId, Integer outletCategoryId, BigDecimal merchantPrice, boolean isApproved) {

        if (!isApproved) return merchantPrice;

        return pricingRepo.findCurrentPrice(productId, outletCategoryId).orElse(merchantPrice);
    }

    private BigDecimal calculateFinalPrice(BigDecimal base, String type, BigDecimal value) {

        if ("FLAT".equalsIgnoreCase(type)) {
            return base.add(value);
        }

        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            return base.add(base.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        throw new PricingException("Invalid pricing type");
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
        dto.setPhotos(product.getPhotos());
        dto.setThumbnail(product.getThumbnail());

        log.info("SERVICE_SUCCESS | GET_PRODUCT_BY_OUTLET | productId={} | outletId={} | onlinePrice={}", productId, outletId, pricing.getOnlinePrice());

        return dto;
    }
}

