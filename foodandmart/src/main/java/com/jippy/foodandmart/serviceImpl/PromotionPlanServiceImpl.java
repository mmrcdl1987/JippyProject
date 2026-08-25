package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.enums.PromotionStatus;
import com.jippy.foodandmart.exception.InvalidPromotionAmountException;
import com.jippy.foodandmart.exception.InvalidPromotionDateException;
import com.jippy.foodandmart.exception.InvalidPromotionItemException;
import com.jippy.foodandmart.exception.PromotionPlanAlreadyExistsException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.PromotionPlanMapper;
import com.jippy.foodandmart.producer.PromotionEventProducer;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IPromotionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jippy.foodandmart.specification.PromotionPlanSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionPlanServiceImpl implements IPromotionPlanService {

    private static final Integer SYSTEM_USER = 1;

    private final PromotionPlanRepository promotionPlanRepository;
    private final PromotionPlanProductRepository promotionPlanProductRepository;
    private final PromotionPlanTypeRepository promotionPlanTypeRepository;
    private final FmOutletRepository outletRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmProductRepository productRepository;
    private final PromotionPlanMapper promotionPlanMapper;
    private final PromotionEventProducer promotionEventProducer;
    private final FmOutletAddressRepository outletAddressRepository;
    private final CacheInvalidateServiceImpl cacheInvalidateService;

    /**
     * Create Promotion Plan
     */
    @Override
    @Transactional
    public PromotionPlanAuditResponseDto createPromotionPlan(PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] Create request received | outletId={} | offerName={} | promotionTypeId={}", requestDto.getOutletId(), requestDto.getOfferName(), requestDto.getPromotionPlanTypeId());

        validatePromotionRequest(requestDto);

        log.debug("[PROMOTION-PLAN] Validating outlet | outletId={}", requestDto.getOutletId());

        FmOutlet outlet = outletRepository.findById(requestDto.getOutletId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Outlet not found | outletId={}", requestDto.getOutletId());

            return new ResourceNotFoundException("Outlet", requestDto.getOutletId());
        });

        log.debug("[PROMOTION-PLAN] Validating promotion plan type | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

        PromotionPlanType promotionPlanType = promotionPlanTypeRepository.findById(requestDto.getPromotionPlanTypeId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan type not found | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

            return new ResourceNotFoundException("Promotion Plan Type", requestDto.getPromotionPlanTypeId());
        });

        validateDuplicateOfferName(requestDto.getOutletId(), requestDto.getOfferName());
        log.debug("[PROMOTION-PLAN] Mapping request DTO to entity | outletId={} | offerName={}", requestDto.getOutletId(), requestDto.getOfferName());

        PromotionPlan promotionPlan = promotionPlanMapper.toEntity(requestDto);

        promotionPlan.setOutletId(outlet.getOutletId());
        promotionPlan.setPromotionPlanType(promotionPlanType);
        promotionPlan.setCreatedBy(SYSTEM_USER);
        promotionPlan.setCreatedAt(LocalDateTime.now());
        log.debug("[PROMOTION-PLAN] Persisting promotion plan | outletId={} | offerName={}", promotionPlan.getOutletId(), promotionPlan.getOfferName());

        PromotionPlan savedPromotionPlan = promotionPlanRepository.save(promotionPlan);

        log.debug("[PROMOTION-PLAN] Promotion plan persisted successfully | promotionPlanId={}", savedPromotionPlan.getPromotionPlanId());

        savePromotionPlanProducts(savedPromotionPlan, requestDto);

        promotionEventProducer.publishPromotionCreated(savedPromotionPlan.getPromotionPlanId());

        PromotionPlanAuditResponseDto response = promotionPlanMapper.toAuditResponseDto(savedPromotionPlan);

        response.setProductIds(requestDto.getProductIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getProductIds())));

        response.setOutletCategoryIds(requestDto.getOutletCategoryIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

        response.setMaxSelection(
                requestDto.getMaxSelection() == null
                        ? -1
                        : requestDto.getMaxSelection()
        );

        log.info("[PROMOTION-PLAN] Promotion plan created successfully | promotionPlanId={} | outletId={} | products={} | categories={}", savedPromotionPlan.getPromotionPlanId(), savedPromotionPlan.getOutletId(), response.getProductIds().size(), response.getOutletCategoryIds().size());

        cacheInvalidateService.invalidateCache(outlet.getOutletId());
        return response;
    }

    /**
     * Get Promotion Plan By Id
     */
    @Override
    public PromotionPlanResponseDto getPromotionPlanById(Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Fetch request received | promotionPlanId={}", promotionPlanId);

        PromotionPlan promotionPlan = promotionPlanRepository.findById(promotionPlanId).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan not found | promotionPlanId={}", promotionPlanId);

            return new ResourceNotFoundException("Promotion Plan", promotionPlanId);
        });

        log.debug("[PROMOTION-PLAN] Mapping promotion plan to response DTO | promotionPlanId={}", promotionPlanId);

        PromotionPlanResponseDto response = promotionPlanMapper.toResponseDto(promotionPlan);

        populateProductsAndCategories(response, promotionPlanId);

        log.info("[PROMOTION-PLAN] Promotion plan fetched successfully | promotionPlanId={} | products={} | categories={}", promotionPlanId, response.getProductIds().size(), response.getOutletCategoryIds().size());

        return response;
    }

    /**
     * Get All Promotion Plans
     */
    @Override
    public List<PromotionPlanResponseDto> getAllPromotionPlans() {

        log.info("[PROMOTION-PLAN] Fetch all promotion plans request received.");

        List<PromotionPlan> promotionPlans = promotionPlanRepository.findAll();

        log.debug("[PROMOTION-PLAN] Total promotion plans found={}", promotionPlans.size());

        List<PromotionPlanResponseDto> responseList = new ArrayList<>(promotionPlans.size());

        for (PromotionPlan promotionPlan : promotionPlans) {

            PromotionPlanResponseDto dto = promotionPlanMapper.toResponseDto(promotionPlan);

            populateProductsAndCategories(dto, promotionPlan.getPromotionPlanId());

            responseList.add(dto);
        }

        log.info("[PROMOTION-PLAN] Successfully fetched {} promotion plans.", responseList.size());

        return responseList;
    }

    /**
     * Update Promotion Plan
     */
    @Override
    @Transactional
    public PromotionPlanAuditResponseDto updatePromotionPlan(Integer promotionPlanId, PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] Update request received | promotionPlanId={} | outletId={} | offerName={}", promotionPlanId, requestDto.getOutletId(), requestDto.getOfferName());

        /*
         * Validate basic request data.
         */
        validateDateAndTime(requestDto);

        validateOfferAmount(requestDto);

        validatePromotionItems(requestDto);

        /*
         * Validate merchant promotion overlap.
         *
         * Current promotionPlanId is excluded from the overlap check.
         */
        validateMerchantPromotionOverlapForUpdate(promotionPlanId, requestDto);

        /*
         * Load existing promotion.
         */
        PromotionPlan promotionPlan = promotionPlanRepository.findById(promotionPlanId).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan not found | promotionPlanId={}", promotionPlanId);

            return new ResourceNotFoundException("Promotion Plan", promotionPlanId);
        });

        /*
         * Validate outlet.
         */
        FmOutlet outlet = outletRepository.findById(requestDto.getOutletId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Outlet not found | outletId={}", requestDto.getOutletId());

            return new ResourceNotFoundException("Outlet", requestDto.getOutletId());
        });

        /*
         * Validate promotion plan type.
         */
        PromotionPlanType promotionPlanType = promotionPlanTypeRepository.findById(requestDto.getPromotionPlanTypeId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan type not found | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

            return new ResourceNotFoundException("Promotion Plan Type", requestDto.getPromotionPlanTypeId());
        });

        /*
         * Validate duplicate offer name.
         */
        validateDuplicateOfferNameForUpdate(requestDto.getOutletId(), requestDto.getOfferName(), promotionPlanId);

        /*
         * Update entity.
         */
        promotionPlanMapper.updateEntity(promotionPlan, requestDto);

        promotionPlan.setOutletId(outlet.getOutletId());

        promotionPlan.setPromotionPlanType(promotionPlanType);

        promotionPlan.setUpdatedBy(SYSTEM_USER);

        promotionPlan.setUpdatedAt(LocalDateTime.now());

        log.debug("[PROMOTION-PLAN] Updating promotion plan | promotionPlanId={}", promotionPlanId);

        PromotionPlan updatedPromotionPlan = promotionPlanRepository.save(promotionPlan);

        /*
         * Remove old product/category mappings.
         */
        log.debug("[PROMOTION-PLAN] Removing existing promotion mappings | promotionPlanId={}", promotionPlanId);

        promotionPlanProductRepository.deleteByPromotionPlanPromotionPlanId(promotionPlanId);

        /*
         * Save new product/category mappings.
         */
        log.debug("[PROMOTION-PLAN] Saving updated promotion mappings | promotionPlanId={}", promotionPlanId);

        savePromotionPlanProducts(updatedPromotionPlan, requestDto);

        /*
         * Publish Kafka event.
         */
        promotionEventProducer.publishPromotionUpdated(updatedPromotionPlan.getPromotionPlanId());

        /*
         * Build response.
         */
        PromotionPlanAuditResponseDto response = promotionPlanMapper.toAuditResponseDto(updatedPromotionPlan);

        response.setProductIds(requestDto.getProductIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getProductIds())));

        response.setOutletCategoryIds(requestDto.getOutletCategoryIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

        response.setMaxSelection(
                requestDto.getMaxSelection() == null
                        ? -1
                        : requestDto.getMaxSelection()
        );

        log.info("[PROMOTION-PLAN] Promotion plan updated successfully | " + "promotionPlanId={} | outletId={} | products={} | categories={}", updatedPromotionPlan.getPromotionPlanId(), updatedPromotionPlan.getOutletId(), response.getProductIds().size(), response.getOutletCategoryIds().size());

        cacheInvalidateService.invalidateCache(outlet.getOutletId());

        return response;
    }

    /**
     * Delete Promotion Plan
     */
    @Override
    @Transactional
    public FmApiResponse<Void> deletePromotionPlan(Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Delete request received | promotionPlanId={}", promotionPlanId);

        PromotionPlan promotionPlan = promotionPlanRepository.findById(promotionPlanId).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan not found | promotionPlanId={}", promotionPlanId);

            return new ResourceNotFoundException("Promotion Plan", promotionPlanId);
        });

        promotionPlanProductRepository.deleteByPromotionPlanPromotionPlanId(promotionPlanId);

        promotionPlanRepository.delete(promotionPlan);

        promotionEventProducer.publishPromotionDeleted(promotionPlanId);

        log.info("[PROMOTION-PLAN] Promotion plan deleted successfully | promotionPlanId={}", promotionPlanId);

        return FmApiResponse.success("Promotion Plan deleted successfully.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public FmApiResponse<PageResponseDto<PromotionListResponseDto>> getPromotionPlans(Integer outletId, PromotionStatus status, int page, int size, String sortBy, String direction) {

        log.info("[PROMOTION-PLAN] Fetch promotion plans | outletId={} | status={}", outletId, status);

        Page<PromotionPlan> promotionPage;

        if (status == null || status == PromotionStatus.ALL) {

            Sort sort = "DESC".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Specification<PromotionPlan> specification = PromotionPlanSpecification.hasOutletId(outletId);

            promotionPage = promotionPlanRepository.findAll(specification, pageable);

        } else {

            Sort sort = "DESC".equalsIgnoreCase(direction) ? Sort.by("promotion_plans_id").descending() : Sort.by("promotion_plans_id").ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            promotionPage = promotionPlanRepository.findByOutletAndStatus(outletId, status.name(), pageable);
        }

        Page<PromotionListResponseDto> responsePage = promotionPage.map(promotionPlanMapper::toPromotionListResponseDto);

        log.info("[PROMOTION-PLAN] Total promotions found={}", responsePage.getTotalElements());

        return FmApiResponse.success("Promotion plans fetched successfully.", PageResponseDto.from(responsePage));
    }

    @Override
    public PromotionStatusCountDto getPromotionStatusCounts(Integer outletId) {

        log.info("[PROMOTION-PLAN] Fetch promotion status counts | outletId={}", outletId);

        List<PromotionPlan> promotionPlans = promotionPlanRepository.findAll(PromotionPlanSpecification.hasOutletId(outletId));

        long active = 0;
        long scheduled = 0;
        long ended = 0;

        for (PromotionPlan promotionPlan : promotionPlans) {

            PromotionStatus status = promotionPlanMapper.toPromotionListResponseDto(promotionPlan).getStatus();

            switch (status) {

                case ACTIVE:
                    active++;
                    break;

                case SCHEDULED:
                    scheduled++;
                    break;

                case ENDED:
                    ended++;
                    break;

                default:
                    break;
            }
        }

        PromotionStatusCountDto dto = new PromotionStatusCountDto();

        dto.setTotal(promotionPlans.size());
        dto.setActive(active);
        dto.setScheduled(scheduled);
        dto.setEnded(ended);

        log.info("[PROMOTION-PLAN] Status counts | total={} | active={} | scheduled={} | ended={}", dto.getTotal(), dto.getActive(), dto.getScheduled(), dto.getEnded());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionScheduleDetailsDto getPromotionScheduleDetails(Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Fetch schedule details | promotionPlanId={}", promotionPlanId);

        PromotionPlan promotionPlan = promotionPlanRepository.findByPromotionPlanId(promotionPlanId).orElseThrow(() -> new ResourceNotFoundException("Promotion plan not found with id: " + promotionPlanId));

        List<PromotionPlanProduct> promotionPlanProducts = promotionPlanProductRepository.findByPromotionPlanPromotionPlanId(promotionPlanId);

        Set<Integer> productIds = new LinkedHashSet<>();
        Set<Integer> categoryIds = new LinkedHashSet<>();

        /*
         * Product / Category based promotion
         */
        for (PromotionPlanProduct promotionPlanProduct : promotionPlanProducts) {

            if (promotionPlanProduct.getProductId() != null) {
                productIds.add(promotionPlanProduct.getProductId());
            }

            if (promotionPlanProduct.getOutletCategoryId() != null) {
                categoryIds.add(promotionPlanProduct.getOutletCategoryId());
            }
        }

        /*
         * Category-based promotion.
         *
         * Resolve products only from the same outlet.
         */
        if (!categoryIds.isEmpty()) {

            List<FmProduct> products = productRepository.findByOutletIdAndOutletCategoryIds(promotionPlan.getOutletId(), new ArrayList<>(categoryIds));

            products.stream().map(FmProduct::getProductId).forEach(productIds::add);
        }

        /*
         * Entire-menu promotion.
         *
         * No promotion_plan_products records means
         * promotion applies to all active products of the outlet.
         */
        if (promotionPlanProducts.isEmpty()) {

            List<Integer> activeProductIds = productRepository.findActiveProductIdsByOutlet(promotionPlan.getOutletId());

            productIds.addAll(activeProductIds);
        }

        /*
         * Resolve outlet area.
         */
        Integer areaId = outletAddressRepository.findByJippyAddressIdAndAddressType(promotionPlan.getOutletId(), "OUTLET").map(FmOutletAddress::getAreaId).orElse(null);

        PromotionScheduleDetailsDto response = new PromotionScheduleDetailsDto();

        response.setPromotionPlanId(promotionPlan.getPromotionPlanId());

        response.setOutletId(promotionPlan.getOutletId());

        response.setAreaId(areaId);

        response.setPlanStartDate(promotionPlan.getPlanStartDate());

        response.setPlanEndDate(promotionPlan.getPlanEndDate());

        response.setPlanStartTime(promotionPlan.getPlanStartTime());

        response.setPlanEndTime(promotionPlan.getPlanEndTime());

        response.setProductIds(new ArrayList<>(productIds));

        log.info("[PROMOTION-PLAN] Schedule details prepared | " + "promotionPlanId={} | outletId={} | areaId={} | productCount={}", promotionPlanId, promotionPlan.getOutletId(), areaId, productIds.size());

        return response;
    }

    /**
     * Save Promotion Products & Categories
     */
    private void savePromotionPlanProducts(PromotionPlan promotionPlan, PromotionPlanRequestDto requestDto) {

        log.debug("[PROMOTION-PLAN] Saving promotion mappings | promotionPlanId={}", promotionPlan.getPromotionPlanId());

        LocalDateTime now = LocalDateTime.now();

        List<PromotionPlanProduct> mappings = new ArrayList<>();

        Set<Integer> productIds = requestDto.getProductIds() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(requestDto.getProductIds());

        for (Integer productId : productIds) {

            PromotionPlanProduct product = new PromotionPlanProduct();

            product.setPromotionPlan(promotionPlan);
            product.setProductId(productId);
            product.setMaxSelection(
                    requestDto.getMaxSelection() == null
                            ? -1
                            : requestDto.getMaxSelection()
            );

            product.setCreatedBy(SYSTEM_USER);
            product.setCreatedAt(now);

            mappings.add(product);
        }

        Set<Integer> categoryIds = requestDto.getOutletCategoryIds() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(requestDto.getOutletCategoryIds());

        for (Integer categoryId : categoryIds) {

            PromotionPlanProduct category = new PromotionPlanProduct();
            category.setPromotionPlan(promotionPlan);
            category.setOutletCategoryId(categoryId);
            category.setMaxSelection(
                    requestDto.getMaxSelection() == null
                            ? -1
                            : requestDto.getMaxSelection()
            );

            category.setCreatedBy(SYSTEM_USER);
            category.setCreatedAt(now);

            mappings.add(category);
        }

        if (!mappings.isEmpty()) {

            promotionPlanProductRepository.saveAll(mappings);

            log.debug("[PROMOTION-PLAN] Saved {} promotion mappings.", mappings.size());
        } else {

            log.debug("[PROMOTION-PLAN] Entire menu promotion. No mappings to persist.");
        }
    }

    /**
     * Populate Product & Category Ids for Response DTO
     */
    private void populateProductsAndCategories(
            PromotionPlanResponseDto dto,
            Integer promotionPlanId) {

        log.debug(
                "[PROMOTION-PLAN] Loading promotion mappings | promotionPlanId={}",
                promotionPlanId);

        List<PromotionPlanProduct> mappings =
                promotionPlanProductRepository
                        .findByPromotionPlanPromotionPlanId(promotionPlanId);

        if (mappings.isEmpty()) {

            dto.setProductIds(new ArrayList<>());
            dto.setOutletCategoryIds(new ArrayList<>());

            // Entire-menu promotion has no promotion_plan_products row.
            dto.setMaxSelection(-1);

            log.debug(
                    "[PROMOTION-PLAN] No mappings found | promotionPlanId={}",
                    promotionPlanId);

            return;
        }

        Set<Integer> productIds = new LinkedHashSet<>();
        Set<Integer> categoryIds = new LinkedHashSet<>();

        Integer maxSelection = -1;

        for (PromotionPlanProduct mapping : mappings) {

            if (mapping.getProductId() != null) {
                productIds.add(mapping.getProductId());
            }

            if (mapping.getOutletCategoryId() != null) {
                categoryIds.add(mapping.getOutletCategoryId());
            }

            /*
             * All mappings belonging to one promotion should have
             * the same maxSelection.
             */
            if (mapping.getMaxSelection() != null) {
                maxSelection = mapping.getMaxSelection();
            }
        }

        dto.setProductIds(new ArrayList<>(productIds));

        dto.setOutletCategoryIds(new ArrayList<>(categoryIds));

        dto.setMaxSelection(maxSelection);

        log.debug(
                "[PROMOTION-PLAN] Loaded promotion mappings | promotionPlanId={} | products={} | categories={} | maxSelection={}",
                promotionPlanId,
                productIds.size(),
                categoryIds.size(),
                maxSelection);
    }

    /**
     * Validate Complete Promotion Request
     */
    private void validatePromotionRequest(PromotionPlanRequestDto requestDto) {

        log.debug("[PROMOTION-PLAN] Validating promotion request.");

        validateDateAndTime(requestDto);

        validateOfferAmount(requestDto);

        validatePromotionItems(requestDto);

        validateMaxSelection(requestDto);

        validateMerchantPromotionOverlap(requestDto);
    }
    private void validateMaxSelection(PromotionPlanRequestDto requestDto) {

        Integer maxSelection = requestDto.getMaxSelection();

        if (maxSelection != null && maxSelection == 0) {
            throw new InvalidPromotionItemException(
                    "Max selection must be -1 or greater than zero."
            );
        }

        if (maxSelection != null && maxSelection < -1) {
            throw new InvalidPromotionItemException(
                    "Max selection must be -1 or greater than zero."
            );
        }
    }

    /**
     * Validate Promotion Dates & Times
     */
    private void validateDateAndTime(PromotionPlanRequestDto requestDto) {

        if (requestDto.getPlanStartDate() == null || requestDto.getPlanEndDate() == null) {

            throw new InvalidPromotionDateException("Promotion start date and end date are required.");
        }

        if (requestDto.getPlanStartTime() == null || requestDto.getPlanEndTime() == null) {

            throw new InvalidPromotionDateException("Promotion start time and end time are required.");
        }

        if (requestDto.getPlanStartDate().isAfter(requestDto.getPlanEndDate())) {

            log.error("[PROMOTION-PLAN] Invalid promotion dates | startDate={} | endDate={}", requestDto.getPlanStartDate(), requestDto.getPlanEndDate());

            throw new InvalidPromotionDateException("Promotion start date cannot be after end date.");
        }

        if (requestDto.getPlanStartDate().isEqual(requestDto.getPlanEndDate()) && requestDto.getPlanStartTime().isAfter(requestDto.getPlanEndTime())) {

            log.error("[PROMOTION-PLAN] Invalid promotion times | startTime={} | endTime={}", requestDto.getPlanStartTime(), requestDto.getPlanEndTime());

            throw new InvalidPromotionDateException("Promotion start time cannot be after end time.");
        }
    }

    /**
     * Validate Offer Amount
     */
    private void validateOfferAmount(PromotionPlanRequestDto requestDto) {

        if (requestDto.getOfferAmount() == null || requestDto.getOfferAmount().signum() <= 0) {

            throw new InvalidPromotionAmountException("Offer amount must be greater than zero.");
        }

        if (requestDto.getMinimumOrderValue() != null && requestDto.getMinimumOrderValue().signum() < 0) {

            throw new InvalidPromotionAmountException("Minimum order value cannot be negative.");
        }

        if ("PERCENTAGE".equalsIgnoreCase(requestDto.getOfferType()) && requestDto.getOfferAmount().compareTo(new BigDecimal("100")) > 0) {

            throw new InvalidPromotionAmountException("Percentage offer cannot exceed 100.");
        }
    }

    /**
     * Validate Product & Category Ownership.
     * <p>
     * Merchant promotions are validated only against the merchant's
     * own outlet data. No validation is performed against admin
     * coupon or price-drop campaigns.
     */
    private void validatePromotionItems(PromotionPlanRequestDto requestDto) {

        Integer outletId = requestDto.getOutletId();

        /*
         * Validate selected outlet categories.
         */
        if (requestDto.getOutletCategoryIds() != null && !requestDto.getOutletCategoryIds().isEmpty()) {

            for (Integer outletCategoryId : new LinkedHashSet<>(requestDto.getOutletCategoryIds())) {

                if (outletCategoryId == null) {
                    throw new InvalidPromotionItemException("Outlet Category Id cannot be null.");
                }

                boolean exists = outletCategoryRepository.existsByOutletCategoryIdAndOutletId(outletCategoryId, outletId);

                if (!exists) {

                    log.error("[PROMOTION-PLAN] Invalid outlet category | outletId={} | outletCategoryId={}", outletId, outletCategoryId);

                    throw new InvalidPromotionItemException("Outlet Category " + outletCategoryId + " does not belong to outlet " + outletId);
                }
            }
        }

        /*
         * Validate selected products.
         */
        if (requestDto.getProductIds() != null && !requestDto.getProductIds().isEmpty()) {

            for (Integer productId : new LinkedHashSet<>(requestDto.getProductIds())) {

                if (productId == null) {
                    throw new InvalidPromotionItemException("Product Id cannot be null.");
                }

                boolean exists = productRepository.existsByProductIdAndOutletId(productId, outletId);

                if (!exists) {

                    log.error("[PROMOTION-PLAN] Invalid product | outletId={} | productId={}", outletId, productId);

                    throw new InvalidPromotionItemException("Product " + productId + " does not belong to outlet " + outletId);
                }
            }
        }
    }

    /**
     * Validate Duplicate Offer Name
     */
    private void validateDuplicateOfferName(Integer outletId, String offerName) {

        promotionPlanRepository.findByOutletIdAndOfferNameIgnoreCase(outletId, offerName).ifPresent(plan -> {

            log.error("[PROMOTION-PLAN] Duplicate offer | outletId={} | offerName={}", outletId, offerName);

            throw new PromotionPlanAlreadyExistsException("Offer name '" + offerName + "' already exists for this outlet.");
        });
    }

    /**
     * Validate Duplicate Offer During Update
     */
    private void validateDuplicateOfferNameForUpdate(Integer outletId, String offerName, Integer promotionPlanId) {

        promotionPlanRepository.findByOutletIdAndOfferNameIgnoreCaseAndPromotionPlanIdNot(outletId, offerName, promotionPlanId).ifPresent(plan -> {

            log.error("[PROMOTION-PLAN] Duplicate offer during update | promotionPlanId={} | outletId={} | offerName={}", promotionPlanId, outletId, offerName);

            throw new PromotionPlanAlreadyExistsException("Offer name '" + offerName + "' already exists for this outlet.");
        });
    }

    /**
     * Validate Merchant Promotion Overlap
     * <p>
     * Checks whether another merchant promotion already exists
     * for the same outlet and product during the requested
     * date/time range.
     */
    private void validateMerchantPromotionOverlap(PromotionPlanRequestDto requestDto) {

        LocalDateTime startDateTime = LocalDateTime.of(requestDto.getPlanStartDate(), requestDto.getPlanStartTime());

        LocalDateTime endDateTime = LocalDateTime.of(requestDto.getPlanEndDate(), requestDto.getPlanEndTime());

        Set<Integer> productIds = new LinkedHashSet<>();

        /*
         * Direct product selections.
         */
        if (requestDto.getProductIds() != null) {

            productIds.addAll(requestDto.getProductIds().stream().filter(productId -> productId != null).toList());
        }

        /*
         * Category selections.
         *
         * Resolve all products belonging to the selected
         * categories of this outlet.
         */
        if (requestDto.getOutletCategoryIds() != null && !requestDto.getOutletCategoryIds().isEmpty()) {

            List<FmProduct> categoryProducts = productRepository.findByOutletIdAndOutletCategoryIds(requestDto.getOutletId(), new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

            categoryProducts.stream().map(FmProduct::getProductId).filter(productId -> productId != null).forEach(productIds::add);
        }

        /*
         * Entire-menu promotion.
         *
         * Empty productIds + empty categoryIds means
         * all active products of the outlet.
         */
        if (productIds.isEmpty()) {

            List<Integer> activeProductIds = productRepository.findActiveProductIdsByOutlet(requestDto.getOutletId());

            activeProductIds.stream().filter(productId -> productId != null).forEach(productIds::add);
        }

        /*
         * Validate every affected product.
         */
        for (Integer productId : productIds) {

            long count = promotionPlanRepository.countMerchantPromotionOverlap(requestDto.getOutletId(), productId, startDateTime, endDateTime);

            if (count > 0) {

                log.error("[PROMOTION-PLAN] Merchant promotion overlap | " + "outletId={} | productId={} | start={} | end={}", requestDto.getOutletId(), productId, startDateTime, endDateTime);

                throw new PromotionPlanAlreadyExistsException("Merchant promotion already exists for product " + productId + " during the selected date and time.");
            }
        }
    }

    /**
     * Validate Merchant Promotion Overlap During Update.
     * <p>
     * The current promotion is excluded from the overlap check.
     */
    private void validateMerchantPromotionOverlapForUpdate(Integer promotionPlanId, PromotionPlanRequestDto requestDto) {

        LocalDateTime startDateTime = LocalDateTime.of(requestDto.getPlanStartDate(), requestDto.getPlanStartTime());

        LocalDateTime endDateTime = LocalDateTime.of(requestDto.getPlanEndDate(), requestDto.getPlanEndTime());

        Set<Integer> productIds = new LinkedHashSet<>();

        /*
         * Direct product selections.
         */
        if (requestDto.getProductIds() != null) {

            requestDto.getProductIds().stream().filter(productId -> productId != null).forEach(productIds::add);
        }

        /*
         * Category selections.
         *
         * Resolve all products belonging to
         * the selected categories.
         */
        if (requestDto.getOutletCategoryIds() != null && !requestDto.getOutletCategoryIds().isEmpty()) {

            List<FmProduct> categoryProducts = productRepository.findByOutletIdAndOutletCategoryIds(requestDto.getOutletId(), new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

            categoryProducts.stream().map(FmProduct::getProductId).filter(productId -> productId != null).forEach(productIds::add);
        }

        /*
         * Entire-menu promotion.
         *
         * No direct products and no categories
         * means all active products.
         */
        if (productIds.isEmpty()) {

            List<Integer> activeProductIds = productRepository.findActiveProductIdsByOutlet(requestDto.getOutletId());

            activeProductIds.stream().filter(productId -> productId != null).forEach(productIds::add);
        }

        /*
         * Check every affected product.
         */
        for (Integer productId : productIds) {

            long count = promotionPlanRepository.countMerchantPromotionOverlapForUpdate(promotionPlanId, requestDto.getOutletId(), productId, startDateTime, endDateTime);

            if (count > 0) {

                log.error("[PROMOTION-PLAN] Merchant promotion overlap during update | " + "promotionPlanId={} | outletId={} | productId={} | " + "start={} | end={}", promotionPlanId, requestDto.getOutletId(), productId, startDateTime, endDateTime);

                throw new PromotionPlanAlreadyExistsException("Another merchant promotion already exists for product " + productId + " during the selected date and time.");
            }
        }
    }
}