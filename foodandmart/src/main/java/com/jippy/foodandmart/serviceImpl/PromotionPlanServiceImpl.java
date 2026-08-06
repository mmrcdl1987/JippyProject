package com.jippy.foodandmart.serviceImpl;

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

    /**
     * Create Promotion Plan
     */
    @Override
    @Transactional
    public PromotionPlanAuditResponseDto createPromotionPlan(PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] Create request received | outletId={} | offerName={} | promotionTypeId={}", requestDto.getOutletId(), requestDto.getOfferName(), requestDto.getPromotionPlanTypeId());

        // Validate request
        validatePromotionRequest(requestDto);

        // Validate duplicate offer
        validateDuplicateOfferName(requestDto.getOutletId(), requestDto.getOfferName());

        // Validate overlapping promotions
        validatePromotionOverlap(requestDto);

        log.debug("[PROMOTION-PLAN] Fetching outlet | outletId={}", requestDto.getOutletId());

        FmOutlet outlet = outletRepository.findById(requestDto.getOutletId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Outlet not found | outletId={}", requestDto.getOutletId());

            return new ResourceNotFoundException("Outlet", requestDto.getOutletId());
        });

        log.debug("[PROMOTION-PLAN] Fetching promotion plan type | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

        PromotionPlanType promotionPlanType = promotionPlanTypeRepository.findById(requestDto.getPromotionPlanTypeId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan type not found | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

            return new ResourceNotFoundException("Promotion Plan Type", requestDto.getPromotionPlanTypeId());
        });

        log.debug("[PROMOTION-PLAN] Mapping request to PromotionPlan entity");

        PromotionPlan promotionPlan = promotionPlanMapper.toEntity(requestDto);

        promotionPlan.setOutletId(outlet.getOutletId());
        promotionPlan.setPromotionPlanType(promotionPlanType);
        promotionPlan.setCreatedBy(SYSTEM_USER);
        promotionPlan.setCreatedAt(LocalDateTime.now());

        PromotionPlan savedPromotionPlan = promotionPlanRepository.save(promotionPlan);

        log.info("[PROMOTION-PLAN] Promotion Plan saved successfully | promotionPlanId={}", savedPromotionPlan.getPromotionPlanId());

        savePromotionPlanProducts(savedPromotionPlan, requestDto);

        log.debug("[PROMOTION-PLAN] Publishing Promotion Created Event | promotionPlanId={}", savedPromotionPlan.getPromotionPlanId());

        promotionEventProducer.publishPromotionCreated(savedPromotionPlan.getPromotionPlanId());

        PromotionPlanAuditResponseDto response = promotionPlanMapper.toAuditResponseDto(savedPromotionPlan);

        response.setProductIds(requestDto.getProductIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getProductIds())));

        response.setOutletCategoryIds(requestDto.getOutletCategoryIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

        log.info("[PROMOTION-PLAN] Promotion created successfully | promotionPlanId={} | outletId={} | productCount={} | categoryCount={}", savedPromotionPlan.getPromotionPlanId(), savedPromotionPlan.getOutletId(), response.getProductIds().size(), response.getOutletCategoryIds().size());

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

        // Validate request
        validatePromotionRequest(requestDto);

        PromotionPlan promotionPlan = promotionPlanRepository.findById(promotionPlanId).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan not found | promotionPlanId={}", promotionPlanId);

            return new ResourceNotFoundException("Promotion Plan", promotionPlanId);
        });

        validateDuplicateOfferNameForUpdate(requestDto.getOutletId(), requestDto.getOfferName(), promotionPlanId);

        validatePromotionOverlapForUpdate(promotionPlanId, requestDto);

        FmOutlet outlet = outletRepository.findById(requestDto.getOutletId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Outlet not found | outletId={}", requestDto.getOutletId());

            return new ResourceNotFoundException("Outlet", requestDto.getOutletId());
        });

        PromotionPlanType promotionPlanType = promotionPlanTypeRepository.findById(requestDto.getPromotionPlanTypeId()).orElseThrow(() -> {

            log.error("[PROMOTION-PLAN] Promotion plan type not found | promotionPlanTypeId={}", requestDto.getPromotionPlanTypeId());

            return new ResourceNotFoundException("Promotion Plan Type", requestDto.getPromotionPlanTypeId());
        });

        promotionPlanMapper.updateEntity(promotionPlan, requestDto);

        promotionPlan.setOutletId(outlet.getOutletId());
        promotionPlan.setPromotionPlanType(promotionPlanType);
        promotionPlan.setUpdatedBy(SYSTEM_USER);
        promotionPlan.setUpdatedAt(LocalDateTime.now());

        PromotionPlan updatedPromotionPlan = promotionPlanRepository.save(promotionPlan);

        promotionPlanProductRepository.deleteByPromotionPlanPromotionPlanId(promotionPlanId);

        savePromotionPlanProducts(updatedPromotionPlan, requestDto);

        promotionEventProducer.publishPromotionUpdated(updatedPromotionPlan.getPromotionPlanId());

        PromotionPlanAuditResponseDto response = promotionPlanMapper.toAuditResponseDto(updatedPromotionPlan);

        response.setProductIds(requestDto.getProductIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getProductIds())));

        response.setOutletCategoryIds(requestDto.getOutletCategoryIds() == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(requestDto.getOutletCategoryIds())));

        log.info("[PROMOTION-PLAN] Promotion updated successfully | promotionPlanId={} | outletId={} | productCount={} | categoryCount={}", updatedPromotionPlan.getPromotionPlanId(), updatedPromotionPlan.getOutletId(), response.getProductIds().size(), response.getOutletCategoryIds().size());

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

        log.debug("[PROMOTION-PLAN] Deleting promotion mappings | promotionPlanId={}", promotionPlanId);

        promotionPlanProductRepository.deleteByPromotionPlanPromotionPlanId(promotionPlanId);

        log.debug("[PROMOTION-PLAN] Deleting promotion plan | promotionPlanId={}", promotionPlanId);

        promotionPlanRepository.delete(promotionPlan);

        log.debug("[PROMOTION-PLAN] Publishing Promotion Deleted Event | promotionPlanId={}", promotionPlanId);

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

        PromotionPlan promotionPlan = promotionPlanRepository.findById(promotionPlanId).orElseThrow(() -> new ResourceNotFoundException("Promotion Plan", promotionPlanId));

        FmOutletAddress outletAddress = outletAddressRepository.findByJippyAddressIdAndAddressType(promotionPlan.getOutletId(), "OUTLET").orElseThrow(() -> new ResourceNotFoundException("Outlet Address", promotionPlan.getOutletId()));

        List<PromotionPlanProduct> promotionPlanProducts = promotionPlanProductRepository.findByPromotionPlanPromotionPlanId(promotionPlanId);

        Set<Integer> productIds = new LinkedHashSet<>();
        List<Integer> categoryIds = new ArrayList<>();

        for (PromotionPlanProduct promotionPlanProduct : promotionPlanProducts) {

            if (promotionPlanProduct.getProductId() != null) {
                productIds.add(promotionPlanProduct.getProductId());
            }

            if (promotionPlanProduct.getOutletCategoryId() != null) {
                categoryIds.add(promotionPlanProduct.getOutletCategoryId());
            }
        }

        if (!categoryIds.isEmpty()) {

            List<FmProduct> products = productRepository.findByOutletIdAndOutletCategoryIds(promotionPlan.getOutletId(), categoryIds);
            products.stream().map(FmProduct::getProductId).forEach(productIds::add);
        }

        if (productIds.isEmpty()) {

            log.error("[PROMOTION-PLAN] No products found for promotionPlanId={}", promotionPlanId);

            throw new ResourceNotFoundException("No products found for promotion plan " + promotionPlanId);
        }

        PromotionScheduleDetailsDto dto = new PromotionScheduleDetailsDto();

        dto.setPromotionPlanId(promotionPlan.getPromotionPlanId());

        dto.setOutletId(promotionPlan.getOutletId());

        dto.setAreaId(outletAddress.getAreaId());

        dto.setPlanStartDate(promotionPlan.getPlanStartDate());

        dto.setPlanEndDate(promotionPlan.getPlanEndDate());

        dto.setPlanStartTime(promotionPlan.getPlanStartTime());

        dto.setPlanEndTime(promotionPlan.getPlanEndTime());

        dto.setProductIds(new ArrayList<>(productIds));

        log.info("[PROMOTION-PLAN] Schedule details prepared | promotionPlanId={} | outletId={} | areaId={} | productCount={}", dto.getPromotionPlanId(), dto.getOutletId(), dto.getAreaId(), dto.getProductIds().size());

        return dto;
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

            product.setCreatedBy(SYSTEM_USER);
            product.setCreatedAt(now);

            mappings.add(product);
        }

        Set<Integer> categoryIds = requestDto.getOutletCategoryIds() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(requestDto.getOutletCategoryIds());

        for (Integer categoryId : categoryIds) {

            PromotionPlanProduct category = new PromotionPlanProduct();

            category.setPromotionPlan(promotionPlan);
            category.setOutletCategoryId(categoryId);

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

    private void validatePromotionOverlap(Integer promotionPlanId, PromotionPlanRequestDto requestDto) {

        Set<Integer> productIds = new LinkedHashSet<>();

        if (requestDto.getProductIds() != null && !requestDto.getProductIds().isEmpty()) {

            productIds.addAll(requestDto.getProductIds());
        }

        if (requestDto.getOutletCategoryIds() != null && !requestDto.getOutletCategoryIds().isEmpty()) {

            List<FmProduct> products = productRepository.findByOutletIdAndOutletCategoryIds(requestDto.getOutletId(), requestDto.getOutletCategoryIds());

            products.stream().map(FmProduct::getProductId).forEach(productIds::add);
        }

        if (productIds.isEmpty()) {

            log.debug("[PROMOTION-PLAN] No products found for overlap validation | outletId={}", requestDto.getOutletId());

            return;
        }

        Long overlapCount;

        if (promotionPlanId == null) {

            overlapCount = promotionPlanProductRepository.countOverlappingProductPromotions(requestDto.getOutletId(), new ArrayList<>(productIds), requestDto.getPlanStartDate(), requestDto.getPlanEndDate());

        } else {

            overlapCount = promotionPlanProductRepository.countOverlappingProductPromotionsForUpdate(promotionPlanId, requestDto.getOutletId(), new ArrayList<>(productIds), requestDto.getPlanStartDate(), requestDto.getPlanEndDate());
        }

        if (overlapCount > 0) {

            log.error("[PROMOTION-PLAN] Overlapping promotion found | outletId={} | overlapCount={}", requestDto.getOutletId(), overlapCount);

            throw new PromotionPlanAlreadyExistsException("Selected products or categories already have an overlapping promotion for the specOne or more selected products already have an active or scheduled promotion during the selected period.ified date range.");
        }

        log.debug("[PROMOTION-PLAN] No overlapping promotion found | outletId={} | productCount={}", requestDto.getOutletId(), productIds.size());
    }

    private void validatePromotionOverlap(PromotionPlanRequestDto requestDto) {

        validatePromotionOverlap(null, requestDto);
    }

    private void validatePromotionOverlapForUpdate(Integer promotionPlanId, PromotionPlanRequestDto requestDto) {

        validatePromotionOverlap(promotionPlanId, requestDto);
    }

    /**
     * Populate Product & Category Ids for Response DTO
     */
    private void populateProductsAndCategories(PromotionPlanResponseDto dto, Integer promotionPlanId) {

        log.debug("[PROMOTION-PLAN] Loading promotion mappings | promotionPlanId={}", promotionPlanId);

        List<PromotionPlanProduct> mappings = promotionPlanProductRepository.findByPromotionPlanPromotionPlanId(promotionPlanId);

        if (mappings.isEmpty()) {

            dto.setProductIds(new ArrayList<>());
            dto.setOutletCategoryIds(new ArrayList<>());

            log.debug("[PROMOTION-PLAN] No mappings found | promotionPlanId={}", promotionPlanId);

            return;
        }

        Set<Integer> productIds = new LinkedHashSet<>();
        Set<Integer> categoryIds = new LinkedHashSet<>();

        for (PromotionPlanProduct mapping : mappings) {

            if (mapping.getProductId() != null) {
                productIds.add(mapping.getProductId());
            }

            if (mapping.getOutletCategoryId() != null) {
                categoryIds.add(mapping.getOutletCategoryId());
            }
        }

        dto.setProductIds(new ArrayList<>(productIds));
        dto.setOutletCategoryIds(new ArrayList<>(categoryIds));

        log.debug("[PROMOTION-PLAN] Loaded promotion mappings | promotionPlanId={} | products={} | categories={}", promotionPlanId, productIds.size(), categoryIds.size());
    }

    /**
     * Validate Complete Promotion Request
     */
    private void validatePromotionRequest(PromotionPlanRequestDto requestDto) {

        log.debug("[PROMOTION-PLAN] Validating promotion request.");

        validateDateAndTime(requestDto);

        validateOfferAmount(requestDto);

        validatePromotionItems(requestDto);
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
        LocalDateTime startDateTime = LocalDateTime.of(requestDto.getPlanStartDate(), requestDto.getPlanStartTime());

        if (startDateTime.isBefore(LocalDateTime.now())) {

            throw new InvalidPromotionDateException("Promotion cannot start in the past.");
        }
    }

    /**
     * Validate Offer Amount
     */
    private void validateOfferAmount(PromotionPlanRequestDto requestDto) {

        if (requestDto.getOfferAmount() == null || requestDto.getOfferAmount().signum() <= 0) {

            throw new InvalidPromotionAmountException("Offer amount must be greater than zero.");
        }

        if (requestDto.getMinimumOrderValue() == null || requestDto.getMinimumOrderValue().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidPromotionAmountException("Minimum order value must be greater than zero.");
        }

        if (!"FLAT".equalsIgnoreCase(requestDto.getOfferType()) && !"PERCENTAGE".equalsIgnoreCase(requestDto.getOfferType())) {

            throw new InvalidPromotionAmountException("Offer type must be FLAT or PERCENTAGE.");
        }
    }

    /**
     * Validate Product & Category Ownership
     */
    private void validatePromotionItems(PromotionPlanRequestDto requestDto) {

        if ((requestDto.getProductIds() == null || requestDto.getProductIds().isEmpty()) && (requestDto.getOutletCategoryIds() == null || requestDto.getOutletCategoryIds().isEmpty())) {

            throw new InvalidPromotionItemException("Select at least one product or category.");
        }

        if (requestDto.getOutletCategoryIds() != null) {

            for (Integer outletCategoryId : new LinkedHashSet<>(requestDto.getOutletCategoryIds())) {

                boolean exists = outletCategoryRepository.existsByOutletCategoryIdAndOutletId(outletCategoryId, requestDto.getOutletId());

                if (!exists) {

                    log.error("[PROMOTION-PLAN] Invalid outlet category | outletId={} | categoryId={}", requestDto.getOutletId(), outletCategoryId);

                    throw new InvalidPromotionItemException("Outlet Category " + outletCategoryId + " does not belong to outlet " + requestDto.getOutletId());
                }
            }
        }

        if (requestDto.getProductIds() != null) {

            for (Integer productId : new LinkedHashSet<>(requestDto.getProductIds())) {

                boolean exists = productRepository.existsByProductIdAndOutletId(productId, requestDto.getOutletId());

                if (!exists) {

                    log.error("[PROMOTION-PLAN] Invalid product | outletId={} | productId={}", requestDto.getOutletId(), productId);

                    throw new InvalidPromotionItemException("Product " + productId + " does not belong to outlet " + requestDto.getOutletId());
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
}