package com.jippy.division.serviceImpl;

import com.jippy.division.dto.*;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.exception.DivInvalidDateException;
import com.jippy.division.exception.DivInvalidRequestException;
import com.jippy.division.exception.DivPromotionScheduleException;
import com.jippy.division.exception.DivResourceNotFoundException;
import com.jippy.division.feignClients.FMFeignClient;
import com.jippy.division.mapper.DivCampaignMapper;
import com.jippy.division.repositary.*;
import com.jippy.division.service.IDivCampaignService;
import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCampaign(DivCampaignRequestDto dto) {

        log.info("Campaign creation started. campaignType={}, locationId={}, outletCount={}, productCount={}", dto.getCampainType(), dto.getLocationId(), dto.getOutletIds().size(), dto.getProductIds() == null ? 0 : dto.getProductIds().size());

        // Step 1 - Basic validations
        validateRequest(dto);

        // Step 2 - Business validations
        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            validateCouponCampaign(dto);

        } else if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            validatePriceDropCampaign(dto);

        } else {

            throw new DivInvalidRequestException("Invalid Campaign Type.");
        }

        // Step 3 - Save Promotion Date
        DivPromotionDate promotionDate = promotionDateRepository.save(DivCampaignMapper.mapToPromotionDateEntity(dto));

        // Step 4 - Save Campaign Mappings
        for (Integer outletId : dto.getOutletIds()) {

            List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

            for (Integer productId : campaignProducts) {

                saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, productId);
            }
        }

        log.info("Campaign created successfully. campaignType={}, promotionDateId={}", dto.getCampainType(), promotionDate.getPromotionDateId());

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

            // Temporary
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

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            validateCouponCampaignForUpdate(campaignId, dto);

        } else if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            validatePriceDropCampaignForUpdate(campaignId, dto);
        }
        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            DivCouponMappingOutletProduct mapping = mappingRepository.findByCouponMappingId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Coupon Mapping not found with id : " + campaignId));

            // Update Promotion Date
            DivPromotionDate promotionDate = promotionDateRepository.findById(mapping.getPromotionDateId()).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found"));

            promotionDate.setPromotionFromDate(LocalDateTime.parse(dto.getPromotionFromDate()));

            promotionDate.setPromotionToDate(LocalDateTime.parse(dto.getPromotionToDate()));

            promotionDate.setMealTypeSlotId(dto.getMealTypeSlotId());

            promotionDateRepository.save(promotionDate);

            // Update Mapping
            mapping.setCouponId(dto.getCouponId());
            mapping.setLocationId(dto.getLocationId());
            mapping.setLocationType(dto.getLocationType());
            mapping.setUpdatedAt(LocalDateTime.now());
            mapping.setUpdatedBy(dto.getCreatedBy());

            mappingRepository.save(mapping);

            promotionScheduleService.updateCouponSchedule(mapping.getCouponMappingId());

            return "Coupon Campaign Updated Successfully";
        }

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            DivPriceDropMappingOutletsProduct mapping = priceDropRepository.findByPriceDropMappingOutletsProductsId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Price Drop Mapping not found with id : " + campaignId));

            DivPromotionDate promotionDate = promotionDateRepository.findById(mapping.getPromotionDateId()).orElseThrow(() -> new DivResourceNotFoundException("Promotion Date not found"));

            promotionDate.setPromotionFromDate(LocalDateTime.parse(dto.getPromotionFromDate()));

            promotionDate.setPromotionToDate(LocalDateTime.parse(dto.getPromotionToDate()));

            promotionDate.setMealTypeSlotId(dto.getMealTypeSlotId());

            promotionDateRepository.save(promotionDate);

            mapping.setPriceModelId(dto.getPriceModelId());
            mapping.setPriceDropValue(dto.getPriceDropValue());
            mapping.setLocationId(dto.getLocationId());
            mapping.setLocationType(dto.getLocationType());
            mapping.setUpdatedAt(LocalDateTime.now());
            mapping.setUpdatedBy(dto.getCreatedBy());

            priceDropRepository.save(mapping);

            promotionScheduleService.updatePriceDropSchedule(mapping.getPriceDropMappingOutletsProductsId());

            return "Price Drop Campaign Updated Successfully";
        }

        throw new DivInvalidRequestException("Invalid Campaign Type");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteCampaign(String campaignType, Integer campaignId) {

        log.info("Campaign delete started. campaignType={}, campaignId={}", campaignType, campaignId);

        if ("COUPON".equalsIgnoreCase(campaignType)) {

            DivCouponMappingOutletProduct mapping = mappingRepository.findByCouponMappingId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Coupon Mapping not found with id : " + campaignId));

            promotionScheduleService.deleteCouponSchedule(campaignId);

            mappingRepository.delete(mapping);

            log.info("Coupon Campaign deleted successfully.");

            return "Coupon Campaign Deleted Successfully";
        }

        if ("PRICE_DROP".equalsIgnoreCase(campaignType)) {

            DivPriceDropMappingOutletsProduct mapping = priceDropRepository.findByPriceDropMappingOutletsProductsId(campaignId).orElseThrow(() -> new DivResourceNotFoundException("Price Drop Mapping not found with id : " + campaignId));

            promotionScheduleService.deletePriceDropSchedule(campaignId);

            priceDropRepository.delete(mapping);

            log.info("Price Drop Campaign deleted successfully.");

            return "Price Drop Campaign Deleted Successfully";
        }

        throw new DivInvalidRequestException("Invalid Campaign Type");
    }

    private void saveCampaign(DivCampaignRequestDto dto, Integer promotionDateId, Integer outletId, Integer productId) {

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            DivCouponMappingOutletProduct mapping = DivCampaignMapper.mapToCouponMappingEntity(dto.getCouponId(), outletId, productId, dto.getLocationId(), dto.getLocationType(), promotionDateId, dto.getCreatedBy());

            mapping = mappingRepository.save(mapping);

            promotionScheduleService.createCouponSchedule(mapping.getCouponMappingId());

            return;
        }

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            DivPriceDropMappingOutletsProduct entity = DivCampaignMapper.mapToPriceDropEntity(outletId, productId, dto.getLocationId(), dto.getLocationType(), promotionDateId, dto.getPriceModelId(), dto.getPriceDropValue(), dto.getCreatedBy());

            entity = priceDropRepository.save(entity);

            promotionScheduleService.createPriceDropSchedule(entity.getPriceDropMappingOutletsProductsId());
        }
    }

    private void validateCampaignForUpdate(Integer campaignId, DivCampaignRequestDto dto) {

        LocalDate fromDate = LocalDateTime.parse(dto.getPromotionFromDate()).toLocalDate();

        LocalDate toDate = LocalDateTime.parse(dto.getPromotionToDate()).toLocalDate();

        Integer campaignCount = 0;

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            campaignCount = mappingRepository.countCouponCampaignForUpdate(campaignId, dto.getLocationId(), dto.getLocationType(), dto.getMealTypeSlotId(), fromDate, toDate);

        } else if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            campaignCount = priceDropRepository.countPriceDropCampaignForUpdate(campaignId, dto.getLocationId(), dto.getLocationType(), dto.getMealTypeSlotId(), fromDate, toDate);
        }

        if (campaignCount > 0) {

            throw new DivPromotionScheduleException("Campaign already exists for selected Location, Date Range and Meal Type.");
        }
    }

    private void validateCoupon(Integer couponId) {

        log.info("Validating coupon. couponId={}", couponId);

        if (!couponRepository.existsByCouponIdAndIsActive(couponId, true)) {

            log.error("Coupon not found or inactive. couponId={}", couponId);

            throw new DivResourceNotFoundException("Coupon not found or inactive.");
        }
    }

    private void validatePriceModel(Integer priceModelId) {

        log.info("Validating Price Model. priceModelId={}", priceModelId);

        if (!priceModelRepository.existsByPriceModelId(priceModelId)) {

            log.error("Price Model not found. priceModelId={}", priceModelId);

            throw new DivResourceNotFoundException("Price Model not found.");
        }
    }

    private void validateRequest(DivCampaignRequestDto dto) {

        if (dto.getCampainType() == null || dto.getCampainType().isBlank()) {
            throw new DivInvalidRequestException("Campaign Type is required");
        }

        if (dto.getLocationId() == null) {
            throw new DivInvalidRequestException("Location is required");
        }

        if (dto.getLocationType() == null || dto.getLocationType().isBlank()) {
            throw new DivInvalidRequestException("Location Type is required");
        }

        if (dto.getMealTypeSlotId() == null) {

            throw new DivInvalidRequestException("Meal Type is required");
        }

        validateMealSlot(dto.getMealTypeSlotId());

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
        for (Integer outletId : dto.getOutletIds()) {

            validateOutletBelongsToLocation(outletId, dto.getLocationId(), dto.getLocationType());
        }

        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {

            for (Integer outletId : dto.getOutletIds()) {

                for (Integer productId : dto.getProductIds()) {

                    validateProductBelongsToOutlet(outletId, productId);
                }
            }
        }

        Set<Integer> uniqueOutletIds = new HashSet<>(dto.getOutletIds());

        if (uniqueOutletIds.size() != dto.getOutletIds().size()) {

            throw new DivInvalidRequestException("Duplicate outlet ids are not allowed.");

        }
        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {

            Set<Integer> uniqueProductIds = new HashSet<>(dto.getProductIds());

            if (uniqueProductIds.size() != dto.getProductIds().size()) {

                throw new DivInvalidRequestException("Duplicate product ids are not allowed.");
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

            log.error("Outlet {} does not belong to {} {}", outletId, locationType, locationId);

            throw new DivInvalidRequestException("Selected outlet does not belong to the selected location.");
        }

        log.info("Outlet location validated successfully. outletId={}", outletId);
    }

    private void validateProductBelongsToOutlet(Integer outletId, Integer productId) {

        log.info("Validating product belongs to outlet. outletId={}, productId={}", outletId, productId);

        Boolean exists = fmClient.existsProductInOutlet(outletId, productId);

        if (Boolean.FALSE.equals(exists)) {

            log.error("Product {} does not belong to outlet {}", productId, outletId);

            throw new DivInvalidRequestException("Product " + productId + " does not belong to outlet " + outletId + ".");
        }

        log.info("Product validated successfully. outletId={}, productId={}", outletId, productId);
    }

    private List<Integer> getCampaignProductIds(Integer outletId, List<Integer> productIds) {

        if (productIds == null || productIds.isEmpty()) {

            log.info("No products selected. Fetching all active products for outletId={}", outletId);

            List<Integer> outletProducts = fmClient.getActiveProductIdsByOutlet(outletId);

            if (outletProducts == null || outletProducts.isEmpty()) {

                throw new DivInvalidRequestException("No active products found for outlet : " + outletId);
            }

            return outletProducts;
        }

        return productIds;
    }

    private void validateCouponOverlap(Integer outletId, Integer productId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        log.info("Validating coupon overlap. outletId={}, productId={}", outletId, productId);

        Long count = mappingRepository.countCouponOverlap(outletId, productId, promotionFromDate, promotionToDate);

        if (count > 0) {

            log.error("Coupon overlap found. outletId={}, productId={}", outletId, productId);

            throw new DivInvalidRequestException("A coupon campaign already exists for the selected product during the selected date range.");
        }

        log.info("Coupon overlap validation passed. outletId={}, productId={}", outletId, productId);
    }

    private void validatePriceDropOverlap(Integer outletId, Integer productId, LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {

        log.info("Validating price drop overlap. outletId={}, productId={}", outletId, productId);

        Long count = priceDropRepository.countPriceDropOverlap(outletId, productId, promotionFromDate, promotionToDate);

        if (count > 0) {

            log.error("Price drop overlap found. outletId={}, productId={}", outletId, productId);

            throw new DivInvalidRequestException("A price drop campaign already exists for the selected product during the selected date range.");
        }

        log.info("Price drop overlap validation passed. outletId={}, productId={}", outletId, productId);
    }

    private void validatePriceDropCampaign(DivCampaignRequestDto dto) {

        log.info("Validating price drop campaign.");

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer outletId : dto.getOutletIds()) {

            List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

            for (Integer productId : campaignProducts) {

                validatePriceDropOverlap(outletId, productId, fromDate, toDate);
            }
        }

        log.info("Price drop campaign validation completed successfully.");
    }

    private void validateCouponCampaign(DivCampaignRequestDto dto) {

        log.info("Validating coupon campaign.");

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer outletId : dto.getOutletIds()) {

            List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

            for (Integer productId : campaignProducts) {

                validateCouponOverlap(outletId, productId, fromDate, toDate);
            }
        }

        log.info("Coupon campaign validation completed successfully.");
    }

    private void validateCouponCampaignForUpdate(Integer campaignId, DivCampaignRequestDto dto) {

        log.info("Validating coupon campaign update.");

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer outletId : dto.getOutletIds()) {

            List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

            for (Integer productId : campaignProducts) {

                Long count = mappingRepository.countCouponOverlapForUpdate(campaignId, outletId, productId, fromDate, toDate);

                if (count > 0) {

                    throw new DivInvalidRequestException("A coupon campaign already exists for the selected product during the selected date range.");
                }
            }
        }

        log.info("Coupon campaign update validation completed successfully.");
    }

    private void validatePriceDropCampaignForUpdate(Integer campaignId, DivCampaignRequestDto dto) {

        log.info("Validating price drop campaign update.");

        LocalDateTime fromDate = LocalDateTime.parse(dto.getPromotionFromDate());

        LocalDateTime toDate = LocalDateTime.parse(dto.getPromotionToDate());

        for (Integer outletId : dto.getOutletIds()) {

            List<Integer> campaignProducts = getCampaignProductIds(outletId, dto.getProductIds());

            for (Integer productId : campaignProducts) {

                Long count = priceDropRepository.countPriceDropOverlapForUpdate(campaignId, outletId, productId, fromDate, toDate);

                if (count > 0) {

                    throw new DivInvalidRequestException("A price drop campaign already exists for the selected product during the selected date range.");
                }
            }
        }

        log.info("Price drop campaign update validation completed successfully.");
    }
}