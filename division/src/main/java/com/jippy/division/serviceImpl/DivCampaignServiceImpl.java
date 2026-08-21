package com.jippy.division.serviceImpl;

import com.jippy.division.dto.*;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.exception.DivInvalidDateException;
import com.jippy.division.exception.DivInvalidRequestException;
import com.jippy.division.exception.DivResourceNotFoundException;
import com.jippy.division.feignClients.FMFeignClient;
import com.jippy.division.mapper.DivCampaignMapper;
import com.jippy.division.projection.DivActiveDiscountsProjection;
import com.jippy.division.repositary.*;
import com.jippy.division.service.IDivCampaignService;
import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivCampaignServiceImpl implements IDivCampaignService {

    private final DivPromotionDateRepository promotionDateRepository;
    private final DivCouponMappingRepository mappingRepository;
    private final DivCouponRepository couponRepository;
    private final DivPriceDropMappingRepository priceDropRepository;
    private final DivPriceModelRepository priceModelRepository;
    private final PromotionScheduleService promotionScheduleService;
    private final FMFeignClient fmClient;
    private final PromotionScheduleRepository promotionScheduleRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCampaign(DivCampaignRequestDto dto) {

        log.info("Campaign creation started. campaignType={}, locationId={}, outletCount={}, productCount={}, mealSlotCount={}", dto.getCampainType(), dto.getLocationId(), dto.getOutletIds() == null ? 0 : dto.getOutletIds().size(), dto.getProductIds() == null ? 0 : dto.getProductIds().size(), dto.getMealTypeSlotIds() == null ? 0 : dto.getMealTypeSlotIds().size());

        validateRequest(dto);

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            validateCouponCampaign(dto);

        } else if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            validatePriceDropCampaign(dto);

        } else {

            throw new DivInvalidRequestException("Invalid Campaign Type.");
        }

        /*
         * One common createdAt identifies all promotion_date
         * records belonging to this campaign.
         */
        LocalDateTime campaignCreatedAt = LocalDateTime.now();

        /*
         * Create one promotion_date record for every meal type slot.
         */
        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            DivPromotionDate promotionDate = DivCampaignMapper.mapToPromotionDateEntity(dto, mealTypeSlotId);

            promotionDate.setCreatedAt(campaignCreatedAt);
            promotionDate.setCreatedBy(dto.getCreatedBy());

            promotionDate = promotionDateRepository.save(promotionDate);

            for (Integer outletId : dto.getOutletIds()) {

                List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                for (Integer productId : campaignProducts) {

                    saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, productId);
                }
            }
        }

        log.info("Campaign created successfully. campaignType={}, mealSlotCount={}", dto.getCampainType(), dto.getMealTypeSlotIds().size());

        return "Campaign Created Successfully";
    }

    @Override
    public List<DivOutletDto> getAvailableOutlets(Integer areaId) {

        log.info("Fetching available outlets by areaId={}", areaId);

        List<DivOutletDto> allOutlets = fmClient.getOutletsByAreaId(areaId);

        List<Integer> couponOutlets = mappingRepository.findActiveCouponOutlets();

        List<Integer> priceDropOutlets = priceDropRepository.findActivePriceDropOutlets();

        Set<Integer> blockedOutlets = new HashSet<>();

        blockedOutlets.addAll(couponOutlets);
        blockedOutlets.addAll(priceDropOutlets);

        return allOutlets.stream().filter(outlet -> !blockedOutlets.contains(outlet.getOutletId())).toList();
    }

    @Override
    public List<AvailableMealSlotResponseDto> getAvailableMealSlots(AvailableMealSlotRequestDto request) {

        log.info("Fetching available meal slots. locationId={}, locationType={}", request.getLocationId(), request.getLocationType());

        List<DivMealTypeTimingResponseDto> mealTypes = fmClient.getAllMealTypeTimings();

        List<AvailableMealSlotResponseDto> response = new ArrayList<>();

        for (DivMealTypeTimingResponseDto mealType : mealTypes) {

            AvailableMealSlotResponseDto dto = new AvailableMealSlotResponseDto();

            dto.setMealTypeTimingsId(mealType.getMealTypeTimingsId());

            dto.setMealType(mealType.getMealType());

            dto.setFromTime(mealType.getFromTime());

            dto.setToTime(mealType.getToTime());

            dto.setAvailable(true);
            dto.setMessage("Available");

            response.add(dto);
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateCampaign(Integer campaignId, DivCampaignRequestDto dto) {

        log.info("Campaign update started. campaignId={}, campaignType={}", campaignId, dto.getCampainType());

        validateRequest(dto);

        /*
         * COUPON UPDATE
         */
        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            /*
             * Validate the new campaign before deleting
             * the existing campaign.
             */
            validateCouponCampaignForUpdate(campaignId, dto);

            DivCouponMappingOutletProduct existingMapping = mappingRepository.findByCouponMappingId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Coupon Mapping not found with id : " + campaignId));

            Integer existingPromotionDateId = existingMapping.getPromotionDateId();

            DivPromotionDate existingPromotionDate = promotionDateRepository.findById(existingPromotionDateId).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + existingPromotionDateId));

            /*
             * All meal slots of the same campaign have
             * the same createdAt.
             */
            List<DivPromotionDate> existingPromotionDates = promotionDateRepository.findByCreatedAt(existingPromotionDate.getCreatedAt());

            /*
             * Collect mappings belonging to ALL meal slots.
             */
            List<DivCouponMappingOutletProduct> existingMappings = new ArrayList<>();

            for (DivPromotionDate promotionDate : existingPromotionDates) {

                existingMappings.addAll(mappingRepository.findByPromotionDateId(promotionDate.getPromotionDateId()));
            }

            /*
             * Delete schedules for all old mappings.
             */
            for (DivCouponMappingOutletProduct mapping : existingMappings) {

                promotionScheduleService.deleteCouponSchedule(mapping.getCouponMappingId());
            }

            /*
             * Delete all old mappings.
             */
            mappingRepository.deleteAll(existingMappings);

            /*
             * Delete all old promotion dates.
             */
            promotionDateRepository.deleteAll(existingPromotionDates);

            /*
             * CREATE UPDATED COUPON CAMPAIGN
             */

            LocalDateTime campaignCreatedAt = LocalDateTime.now();

            for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

                DivPromotionDate promotionDate = DivCampaignMapper.mapToPromotionDateEntity(dto, mealTypeSlotId);

                promotionDate.setCreatedAt(campaignCreatedAt);

                promotionDate.setCreatedBy(dto.getCreatedBy());

                promotionDate = promotionDateRepository.save(promotionDate);

                for (Integer outletId : dto.getOutletIds()) {

                    List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                    for (Integer productId : campaignProducts) {

                        saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, productId);
                    }
                }
            }

            log.info("Coupon campaign updated successfully. campaignId={}", campaignId);

            return "Coupon Campaign Updated Successfully";
        }
        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            /*
             * Validate the new campaign before deleting
             * the existing campaign.
             */
            validatePriceDropCampaignForUpdate(campaignId, dto);

            DivPriceDropMappingOutletsProduct existingMapping = priceDropRepository.findByPriceDropMappingOutletsProductsId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Price Drop Mapping not found with id : " + campaignId));

            Integer existingPromotionDateId = existingMapping.getPromotionDateId();

            DivPromotionDate existingPromotionDate = promotionDateRepository.findById(existingPromotionDateId).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + existingPromotionDateId));

            /*
             * Find ALL promotion_date records belonging
             * to this campaign.
             */
            List<DivPromotionDate> existingPromotionDates = promotionDateRepository.findByCreatedAt(existingPromotionDate.getCreatedAt());

            /*
             * Collect Price Drop mappings from ALL
             * meal slots.
             */
            List<DivPriceDropMappingOutletsProduct> existingMappings = new ArrayList<>();

            for (DivPromotionDate promotionDate : existingPromotionDates) {

                existingMappings.addAll(priceDropRepository.findByPromotionDateId(promotionDate.getPromotionDateId()));
            }

            /*
             * Delete schedules for ALL old mappings.
             */
            for (DivPriceDropMappingOutletsProduct mapping : existingMappings) {

                promotionScheduleService.deletePriceDropSchedule(mapping.getPriceDropMappingOutletsProductsId());
            }

            /*
             * Delete ALL old Price Drop mappings.
             */
            priceDropRepository.deleteAll(existingMappings);

            /*
             * Delete ALL old promotion dates.
             */
            promotionDateRepository.deleteAll(existingPromotionDates);

            /*
             * CREATE UPDATED PRICE DROP CAMPAIGN
             */

            LocalDateTime campaignCreatedAt = LocalDateTime.now();

            for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

                DivPromotionDate promotionDate = DivCampaignMapper.mapToPromotionDateEntity(dto, mealTypeSlotId);

                promotionDate.setCreatedAt(campaignCreatedAt);

                promotionDate.setCreatedBy(dto.getCreatedBy());

                promotionDate = promotionDateRepository.save(promotionDate);

                for (Integer outletId : dto.getOutletIds()) {

                    List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                    for (Integer productId : campaignProducts) {

                        saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, productId);
                    }
                }
            }

            log.info("Price Drop campaign updated successfully. campaignId={}", campaignId);

            return "Price Drop Campaign Updated Successfully";
        }

        throw new DivInvalidRequestException("Invalid Campaign Type");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteCampaign(String campaignType, Integer campaignId) {

        log.info("Campaign delete started. campaignType={}, campaignId={}", campaignType, campaignId);

        /*
         * COUPON DELETE
         */
        if ("COUPON".equalsIgnoreCase(campaignType)) {

            DivCouponMappingOutletProduct mapping = mappingRepository.findByCouponMappingId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Coupon Mapping not found with id : " + campaignId));

            /*
             * Get one promotion_date from the selected mapping.
             */
            Integer promotionDateId = mapping.getPromotionDateId();

            DivPromotionDate promotionDate = promotionDateRepository.findById(promotionDateId).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + promotionDateId));

            /*
             * Find ALL promotion_date records belonging
             * to this campaign.
             *
             * All meal slots created for the same campaign
             * have the same createdAt.
             */
            List<DivPromotionDate> promotionDates = promotionDateRepository.findByCreatedAt(promotionDate.getCreatedAt());

            /*
             * Collect mappings from ALL meal slots.
             */
            List<DivCouponMappingOutletProduct> mappings = new ArrayList<>();

            for (DivPromotionDate campaignPromotionDate : promotionDates) {

                mappings.addAll(mappingRepository.findByPromotionDateId(campaignPromotionDate.getPromotionDateId()));
            }

            /*
             * Delete schedules for ALL mappings.
             */
            for (DivCouponMappingOutletProduct couponMapping : mappings) {

                promotionScheduleService.deleteCouponSchedule(couponMapping.getCouponMappingId());
            }

            /*
             * Delete ALL coupon mappings.
             */
            mappingRepository.deleteAll(mappings);

            /*
             * Delete ALL promotion dates.
             */
            promotionDateRepository.deleteAll(promotionDates);

            log.info("Coupon campaign deleted successfully. campaignId={}, mealSlotCount={}", campaignId, promotionDates.size());

            return "Coupon Campaign Deleted Successfully";
        }

        /*
         * PRICE DROP DELETE
         */
        if ("PRICE_DROP".equalsIgnoreCase(campaignType)) {

            DivPriceDropMappingOutletsProduct mapping = priceDropRepository.findByPriceDropMappingOutletsProductsId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Price Drop Mapping not found with id : " + campaignId));

            /*
             * Get one promotion_date from the selected mapping.
             */
            Integer promotionDateId = mapping.getPromotionDateId();

            DivPromotionDate promotionDate = promotionDateRepository.findById(promotionDateId).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + promotionDateId));

            /*
             * Find ALL promotion_date records belonging
             * to this campaign.
             */
            List<DivPromotionDate> promotionDates = promotionDateRepository.findByCreatedAt(promotionDate.getCreatedAt());

            /*
             * Collect Price Drop mappings from ALL
             * meal slots.
             */
            List<DivPriceDropMappingOutletsProduct> mappings = new ArrayList<>();

            for (DivPromotionDate campaignPromotionDate : promotionDates) {

                mappings.addAll(priceDropRepository.findByPromotionDateId(campaignPromotionDate.getPromotionDateId()));
            }

            /*
             * Delete schedules for ALL mappings.
             */
            for (DivPriceDropMappingOutletsProduct priceDropMapping : mappings) {

                promotionScheduleService.deletePriceDropSchedule(priceDropMapping.getPriceDropMappingOutletsProductsId());
            }

            /*
             * Delete ALL Price Drop mappings.
             */
            priceDropRepository.deleteAll(mappings);

            /*
             * Delete ALL promotion dates.
             */
            promotionDateRepository.deleteAll(promotionDates);

            log.info("Price Drop campaign deleted successfully. campaignId={}, mealSlotCount={}", campaignId, promotionDates.size());

            return "Price Drop Campaign Deleted Successfully";
        }

        throw new DivInvalidRequestException("Invalid Campaign Type");
    }

    private void saveCampaign(DivCampaignRequestDto dto, Integer promotionDateId, Integer outletId, Integer productId) {

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            DivCouponMappingOutletProduct mapping = DivCampaignMapper.mapToCouponMappingEntity(dto.getCouponId(), outletId, productId, dto.getLocationId(), dto.getLocationType(), promotionDateId, dto.getPromotionMessage(), dto.getMaxSelection(), dto.getCreatedBy());
            mapping = mappingRepository.save(mapping);

            promotionScheduleService.createCouponSchedule(mapping.getCouponMappingId());

            return;
        }

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            DivPriceDropMappingOutletsProduct entity = DivCampaignMapper.mapToPriceDropEntity(outletId, productId, dto.getLocationId(), dto.getLocationType(), promotionDateId, dto.getPriceModelId(), dto.getPriceDropValue(), dto.getPromotionMessage(), dto.getMaxSelection(), dto.getCreatedBy());
            entity = priceDropRepository.save(entity);

            promotionScheduleService.createPriceDropSchedule(entity.getPriceDropMappingOutletsProductsId());
        }
    }

    private void validateRequest(DivCampaignRequestDto dto) {

        if (dto.getCampainType() == null || dto.getCampainType().isBlank()) {

            throw new DivInvalidRequestException("Campaign Type is required");
        }

        if (dto.getLocationId() == null) {

            throw new DivInvalidRequestException("Location is required");
        }

        validateMaxSelection(dto);


        if (dto.getLocationType() == null || dto.getLocationType().isBlank()) {

            throw new DivInvalidRequestException("Location Type is required");
        }

        /*
         * New multi-slot validation.
         */
        if (dto.getMealTypeSlotIds() == null || dto.getMealTypeSlotIds().isEmpty()) {

            throw new DivInvalidRequestException("At least one Meal Type Slot is required");
        }

        Set<Integer> uniqueMealSlotIds = new HashSet<>(dto.getMealTypeSlotIds());

        if (uniqueMealSlotIds.size() != dto.getMealTypeSlotIds().size()) {

            throw new DivInvalidRequestException("Duplicate meal type slot ids are not allowed.");
        }

        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            if (mealTypeSlotId == null || mealTypeSlotId <= 0) {

                throw new DivInvalidRequestException("Invalid Meal Type Slot Id.");
            }

            validateMealSlot(mealTypeSlotId);
        }

        if (dto.getPromotionFromDate() == null || dto.getPromotionFromDate().isBlank()) {

            throw new DivInvalidRequestException("Promotion From Date is required");
        }

        if (dto.getPromotionToDate() == null || dto.getPromotionToDate().isBlank()) {

            throw new DivInvalidRequestException("Promotion To Date is required");
        }

        LocalDateTime promotionFromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime promotionToDate = LocalDateTime.parse(dto.getPromotionToDate());

        if (!promotionFromDate.isBefore(promotionToDate)) {

            throw new DivInvalidDateException("Promotion From Date must be before Promotion To Date.");
        }

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new DivInvalidRequestException("Please select at least one outlet");
        }

        Set<Integer> uniqueOutletIds = new HashSet<>(dto.getOutletIds());

        if (uniqueOutletIds.size() != dto.getOutletIds().size()) {

            throw new DivInvalidRequestException("Duplicate outlet ids are not allowed.");
        }

        for (Integer outletId : dto.getOutletIds()) {

            validateOutletBelongsToLocation(outletId, dto.getLocationId(), dto.getLocationType());
        }

        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {

            Set<Integer> uniqueProductIds = new HashSet<>(dto.getProductIds());

            if (uniqueProductIds.size() != dto.getProductIds().size()) {

                throw new DivInvalidRequestException("Duplicate product ids are not allowed.");
            }

            for (Integer outletId : dto.getOutletIds()) {

                for (Integer productId : dto.getProductIds()) {

                    validateProductBelongsToOutlet(outletId, productId);
                }
            }
        }

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            if (dto.getCouponId() == null) {

                throw new DivInvalidRequestException("Coupon is required");
            }

            if (dto.getCouponId() <= 0) {

                throw new DivInvalidRequestException("Invalid Coupon Id.");
            }

            validateCoupon(dto.getCouponId());
        }

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            if (dto.getPriceModelId() == null) {

                throw new DivInvalidRequestException("Price Model is required");
            }

            if (dto.getPriceModelId() <= 0) {

                throw new DivInvalidRequestException("Invalid Price Model Id.");
            }

            validatePriceModel(dto.getPriceModelId());

            if (dto.getPriceDropValue() == null) {

                throw new DivInvalidRequestException("Price Drop Value is required");
            }

            if (dto.getPriceDropValue() <= 0) {

                throw new DivInvalidRequestException("Price Drop Value must be greater than zero.");
            }
        }
    }

    private void validateMealSlot(Integer mealTypeSlotId) {

        log.info("Validating Meal Slot. mealTypeSlotId={}", mealTypeSlotId);

        Boolean exists = fmClient.existsMealTypeTiming(mealTypeSlotId);

        if (Boolean.FALSE.equals(exists)) {

            log.error("Meal Slot not found. mealTypeSlotId={}", mealTypeSlotId);

            throw new DivResourceNotFoundException("Meal Slot not found.");
        }

        log.info("Meal Slot validated successfully. mealTypeSlotId={}", mealTypeSlotId);
    }

    private void validateOutletBelongsToLocation(Integer outletId, Integer locationId, String locationType) {

        log.info("Validating outlet belongs to location. outletId={}, locationType={}, locationId={}", outletId, locationType, locationId);

        DivOutletDetailsDto outlet = fmClient.getOutletDetails(outletId, "MERCHANT", null);

        if (outlet == null) {

            throw new DivResourceNotFoundException("Outlet not found.");
        }

        boolean valid = switch (locationType.toUpperCase()) {

            case "AREA" -> locationId.equals(outlet.getAreaId());

            case "CITY" -> locationId.equals(outlet.getCityId());

            case "STATE" -> locationId.equals(outlet.getStateId());

            default -> throw new DivInvalidRequestException("Invalid Location Type.");
        };

        if (!valid) {

            throw new DivInvalidRequestException("Selected outlet does not belong to the selected location.");
        }
    }

    private void validateProductBelongsToOutlet(Integer outletId, Integer productId) {

        Boolean exists = fmClient.existsProductInOutlet(outletId, productId);

        if (Boolean.FALSE.equals(exists)) {

            throw new DivInvalidRequestException("Product " + productId + " does not belong to outlet " + outletId + ".");
        }
    }

    private List<Integer> getCampaignProductIds(Integer outletId, List<Integer> productIds) {

        if (productIds == null || productIds.isEmpty()) {

            List<Integer> outletProducts = fmClient.getActiveProductIdsByOutlet(outletId);

            if (outletProducts == null || outletProducts.isEmpty()) {

                throw new DivInvalidRequestException("No active products found for outlet : " + outletId);
            }

            return outletProducts;
        }

        return productIds;
    }

    private void validateCoupon(Integer couponId) {

        if (!couponRepository.existsByCouponIdAndIsActive(couponId, true)) {

            throw new DivResourceNotFoundException("Coupon not found or inactive.");
        }
    }

    private void validatePriceModel(Integer priceModelId) {

        if (!priceModelRepository.existsByPriceModelId(priceModelId)) {

            throw new DivResourceNotFoundException("Price Model not found.");
        }
    }

    private void validateCouponOverlap(Integer outletId, Integer productId, Integer mealTypeSlotId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        Long count = mappingRepository.countCouponOverlap(outletId, productId, mealTypeSlotId, promotionFromDate, promotionToDate);

        if (count > 0) {
            throw new DivInvalidRequestException("A coupon campaign already exists for the selected product, " + "meal slot and date range.");
        }
    }

    private void validatePriceDropOverlap(Integer outletId, Integer productId, Integer mealTypeSlotId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        Long count = priceDropRepository.countPriceDropOverlap(outletId, productId, mealTypeSlotId, promotionFromDate, promotionToDate);

        if (count > 0) {
            throw new DivInvalidRequestException("A price drop campaign already exists for the selected product, " + "meal slot and date range.");
        }
    }

    private void validateCouponCampaign(DivCampaignRequestDto dto) {

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            for (Integer outletId : dto.getOutletIds()) {

                List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                for (Integer productId : campaignProducts) {

                    // Coupon vs Coupon
                    validateCouponOverlap(outletId, productId, mealTypeSlotId, fromDate, toDate);

                    // Coupon vs Price Drop
                    validatePriceDropConflictForCoupon(outletId, productId, mealTypeSlotId, fromDate, toDate);
                }
            }
        }
    }

    private void validatePriceDropCampaign(DivCampaignRequestDto dto) {

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            for (Integer outletId : dto.getOutletIds()) {

                List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                for (Integer productId : campaignProducts) {

                    // Price Drop vs Price Drop
                    validatePriceDropOverlap(outletId, productId, mealTypeSlotId, fromDate, toDate);

                    // Price Drop vs Coupon
                    validateCouponConflictForPriceDrop(outletId, productId, mealTypeSlotId, fromDate, toDate);
                }
            }
        }
    }

    private void validatePriceDropConflictForCoupon(Integer outletId, Integer productId, Integer mealTypeSlotId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        Long count = priceDropRepository.countPriceDropOverlap(outletId, productId, mealTypeSlotId, promotionFromDate, promotionToDate);

        if (count > 0) {
            throw new DivInvalidRequestException("A price drop campaign already exists for the selected product, " + "meal slot and date range. Coupon cannot be created.");
        }
    }

    private void validateCouponConflictForPriceDrop(Integer outletId, Integer productId, Integer mealTypeSlotId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        Long count = mappingRepository.countCouponOverlap(outletId, productId, mealTypeSlotId, promotionFromDate, promotionToDate);

        if (count > 0) {
            throw new DivInvalidRequestException("A coupon campaign already exists for the selected product, " + "meal slot and date range. Price drop cannot be created.");
        }
    }

    private void validateCouponCampaignForUpdate(Integer campaignId, DivCampaignRequestDto dto) {

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        DivCouponMappingOutletProduct existingMapping = mappingRepository.findByCouponMappingId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Coupon Mapping not found with id : " + campaignId));

        DivPromotionDate existingPromotionDate = promotionDateRepository.findById(existingMapping.getPromotionDateId()).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + existingMapping.getPromotionDateId()));

        LocalDateTime campaignCreatedAt = existingPromotionDate.getCreatedAt();

        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            for (Integer outletId : dto.getOutletIds()) {

                List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                for (Integer productId : campaignProducts) {

                    Long couponCount = mappingRepository.countCouponOverlapForUpdate(outletId, productId, mealTypeSlotId, fromDate, toDate, campaignCreatedAt);

                    if (couponCount > 0) {

                        throw new DivInvalidRequestException("A coupon campaign already exists for the selected " + "product, meal slot and date range.");
                    }

                    Long priceDropCount = priceDropRepository.countPriceDropOverlap(outletId, productId, mealTypeSlotId, fromDate, toDate);

                    if (priceDropCount > 0) {

                        throw new DivInvalidRequestException("A price drop campaign already exists for the selected " + "product, meal slot and date range. " + "Coupon cannot be updated.");
                    }
                }
            }
        }
    }

    private void validateMaxSelection(DivCampaignRequestDto dto) {

        Integer maxSelection = dto.getMaxSelection();

        if (maxSelection == null) {
            return;
        }

        if (maxSelection == 0 || maxSelection < -1) {
            throw new DivInvalidRequestException("Max selection must be -1 or greater than zero.");
        }
    }

    private void validatePriceDropCampaignForUpdate(Integer campaignId, DivCampaignRequestDto dto) {

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        DivPriceDropMappingOutletsProduct existingMapping = priceDropRepository.findByPriceDropMappingOutletsProductsId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Price Drop Mapping not found with id : " + campaignId));

        DivPromotionDate existingPromotionDate = promotionDateRepository.findById(existingMapping.getPromotionDateId()).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found with id : " + existingMapping.getPromotionDateId()));

        LocalDateTime campaignCreatedAt = existingPromotionDate.getCreatedAt();

        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {

            for (Integer outletId : dto.getOutletIds()) {

                List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

                for (Integer productId : campaignProducts) {

                    Long priceDropCount = priceDropRepository.countPriceDropOverlapForUpdate(outletId, productId, mealTypeSlotId, fromDate, toDate, campaignCreatedAt);

                    if (priceDropCount > 0) {

                        throw new DivInvalidRequestException("A price drop campaign already exists for the selected " + "product, meal slot and date range.");
                    }

                    Long couponCount = mappingRepository.countCouponOverlap(outletId, productId, mealTypeSlotId, fromDate, toDate);

                    if (couponCount > 0) {

                        throw new DivInvalidRequestException("A coupon campaign already exists for the selected " + "product, meal slot and date range. " + "Price Drop cannot be updated.");
                    }
                }
            }
        }
    }

    @Override
    public List<DivActiveDiscountsResponseDto> getActiveDiscounts() {

        List<DivActiveDiscountsProjection> activeDiscountsProjectionList = promotionScheduleRepository.getActiveDiscounts(LocalDateTime.now());

        List<DivActiveDiscountsResponseDto> activeDiscountsResponseDtos = new ArrayList<>();

        for (DivActiveDiscountsProjection projection : activeDiscountsProjectionList) {

            DivActiveDiscountsResponseDto response = new DivActiveDiscountsResponseDto();

            activeDiscountsResponseDtos.add(DivCampaignMapper.mapToActiveDiscounts(projection, response));
        }

        return activeDiscountsResponseDtos;
    }

}