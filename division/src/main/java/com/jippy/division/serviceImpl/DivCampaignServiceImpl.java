package com.jippy.division.serviceImpl;

import com.jippy.division.dto.*;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.feignClients.FMFeignClient;
import com.jippy.division.mapper.DivCampaignMapper;
import com.jippy.division.repositary.DivCouponMappingRepository;
import com.jippy.division.repositary.DivPriceDropMappingRepository;
import com.jippy.division.repositary.DivPromotionDateRepository;
import com.jippy.division.service.IDivCampaignService;
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
    private final DivPriceDropMappingRepository priceDropRepository;
    private final FMFeignClient fmClient;

    /**
     * Performs 3-Phase validation directly using division DB tables (STATE, CITY, OUTLET)
     */
    private void validateCampaign(DivCampaignRequestDto dto) {
        LocalDate fromDate = parseToLocalDate(dto.getPromotionFromDate());
        LocalDate toDate = parseToLocalDate(dto.getPromotionToDate());

        List<Integer> slotIds = dto.getMealTypeSlotIds();
        List<Integer> outletIds = dto.getOutletIds();

        if (slotIds != null) {
            for (Integer slotId : slotIds) {

                // PHASE 1 & 2: Location-level check (STATE or CITY)
                if (dto.getLocationId() != null && dto.getLocationType() != null) {
                    Integer locCouponCount = mappingRepository.countCouponCampaignForLocation(
                            dto.getLocationId(), dto.getLocationType(), slotId, fromDate, toDate);

                    Integer locPriceDropCount = priceDropRepository.countPriceDropCampaignForLocation(
                            dto.getLocationId(), dto.getLocationType(), slotId, fromDate, toDate);

                    if ((locCouponCount != null && locCouponCount > 0) ||
                            (locPriceDropCount != null && locPriceDropCount > 0)) {
                        throw new RuntimeException("A campaign is already running for " + dto.getLocationType()
                                + " for slot ID " + slotId + " during this date range.");
                    }
                }

                // PHASE 3: Outlet-level check
                if (outletIds != null && !outletIds.isEmpty()) {
                    Integer outletCouponCount = mappingRepository.countCouponCampaignForOutlets(
                            outletIds, slotId, fromDate, toDate);

                    Integer outletPriceDropCount = priceDropRepository.countPriceDropCampaignForOutlets(
                            outletIds, slotId, fromDate, toDate);

                    if ((outletCouponCount != null && outletCouponCount > 0) ||
                            (outletPriceDropCount != null && outletPriceDropCount > 0)) {
                        throw new RuntimeException("One or more selected outlets already have an active campaign for slot ID "
                                + slotId + " during this date range.");
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCampaign(DivCampaignRequestDto dto) {
        log.info("Campaign creation started");

        validateRequest(dto);
        validateCampaign(dto);

        // Loop over each selected meal slot ID
        for (Integer mealTypeSlotId : dto.getMealTypeSlotIds()) {
            dto.setMealTypeSlotId(mealTypeSlotId);

            DivPromotionDate promotionDate = DivCampaignMapper.mapToPromotionDateEntity(dto);
            promotionDate = promotionDateRepository.save(promotionDate);

            for (Integer outletId : dto.getOutletIds()) {
                if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
                    for (Integer productId : dto.getProductIds()) {
                        saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, productId);
                    }
                } else {
                    saveCampaign(dto, promotionDate.getPromotionDateId(), outletId, null);
                }
            }
        }

        log.info("Campaign created successfully");
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

        return allOutlets.stream()
                .filter(outlet -> !blockedOutlets.contains(outlet.getOutletId()))
                .toList();
    }

    @Override
    public List<AvailableMealSlotResponseDto> getAvailableMealSlots(AvailableMealSlotRequestDto request) {
        log.info("Fetching meal types from FM client and checking booking status across location and outlets");

        List<DivMealTypeTimingResponseDto> mealTypes = fmClient.getAllMealTypeTimings();
        List<AvailableMealSlotResponseDto> response = new ArrayList<>();

        LocalDate fromDate = request.getPromotionFromDate();
        LocalDate toDate = request.getPromotionToDate();
        List<Integer> outletIds = request.getOutletIds();

        for (DivMealTypeTimingResponseDto mealType : mealTypes) {
            AvailableMealSlotResponseDto dto = new AvailableMealSlotResponseDto();

            dto.setMealTypeTimingsId(mealType.getMealTypeTimingsId());
            dto.setMealType(mealType.getMealType());
            dto.setFromTime(mealType.getFromTime());
            dto.setToTime(mealType.getToTime());

            boolean isBooked = false;

            if (fromDate != null && toDate != null) {

                // Check 1: STATE or CITY level booking check
                if (request.getLocationId() != null && request.getLocationType() != null) {
                    Integer locCouponCount = mappingRepository.countCouponCampaignForLocation(
                            request.getLocationId(), request.getLocationType(), mealType.getMealTypeTimingsId(), fromDate, toDate);

                    Integer locPriceDropCount = priceDropRepository.countPriceDropCampaignForLocation(
                            request.getLocationId(), request.getLocationType(), mealType.getMealTypeTimingsId(), fromDate, toDate);

                    isBooked = (locCouponCount != null && locCouponCount > 0) ||
                            (locPriceDropCount != null && locPriceDropCount > 0);
                }

                // Check 2: OUTLET level booking check
                if (!isBooked && outletIds != null && !outletIds.isEmpty()) {
                    Integer outletCouponCount = mappingRepository.countCouponCampaignForOutlets(
                            outletIds, mealType.getMealTypeTimingsId(), fromDate, toDate);

                    Integer outletPriceDropCount = priceDropRepository.countPriceDropCampaignForOutlets(
                            outletIds, mealType.getMealTypeTimingsId(), fromDate, toDate);

                    isBooked = (outletCouponCount != null && outletCouponCount > 0) ||
                            (outletPriceDropCount != null && outletPriceDropCount > 0);
                }
            }

            if (isBooked) {
                dto.setAvailable(false);
                dto.setMessage("Already Booked");
            } else {
                dto.setAvailable(true);
                dto.setMessage("Available");
            }

            response.add(dto);
        }

        return response;
    }

    private void saveCampaign(DivCampaignRequestDto dto, Integer promotionDateId, Integer outletId, Integer productId) {
        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {
            DivCouponMappingOutletProduct mapping = DivCampaignMapper.mapToCouponMappingEntity(
                    dto.getCouponId(), outletId, productId, dto.getLocationId(),
                    dto.getLocationType(), promotionDateId, dto.getCreatedBy());
            mappingRepository.save(mapping);
            return;
        }

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {
            DivPriceDropMappingOutletsProduct entity = DivCampaignMapper.mapToPriceDropEntity(
                    outletId, productId, dto.getLocationId(), dto.getLocationType(),
                    promotionDateId, dto.getPriceModelId(), dto.getPriceDropValue(), dto.getCreatedBy());
            priceDropRepository.save(entity);
        }
    }

    private void validateRequest(DivCampaignRequestDto dto) {
        if (dto.getCampainType() == null || dto.getCampainType().isBlank()) {
            throw new RuntimeException("Campaign Type is required");
        }
        if (dto.getLocationId() == null) {
            throw new RuntimeException("Location is required");
        }
        if (dto.getLocationType() == null || dto.getLocationType().isBlank()) {
            throw new RuntimeException("Location Type is required");
        }
        if (dto.getMealTypeSlotIds() == null || dto.getMealTypeSlotIds().isEmpty()) {
            throw new RuntimeException("At least one Meal Type slot is required");
        }
        if (dto.getPromotionFromDate() == null || dto.getPromotionFromDate().isBlank()) {
            throw new RuntimeException("Promotion From Date is required");
        }
        if (dto.getPromotionToDate() == null || dto.getPromotionToDate().isBlank()) {
            throw new RuntimeException("Promotion To Date is required");
        }
        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {
            throw new RuntimeException("Please select at least one outlet");
        }
        if ("COUPON".equalsIgnoreCase(dto.getCampainType()) && dto.getCouponId() == null) {
            throw new RuntimeException("Coupon is required");
        }
        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {
            if (dto.getPriceModelId() == null) {
                throw new RuntimeException("Price Model is required");
            }
            if (dto.getPriceDropValue() == null) {
                throw new RuntimeException("Price Drop Value is required");
            }
        }
    }

    private LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return dateStr.contains("T")
                ? LocalDateTime.parse(dateStr).toLocalDate()
                : LocalDate.parse(dateStr);
    }
}