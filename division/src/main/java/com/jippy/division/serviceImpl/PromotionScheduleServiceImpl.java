package com.jippy.division.serviceImpl;

import com.jippy.division.dto.MerchantPromotionDetailsDto;
import com.jippy.division.dto.PromotionCancelRequestDto;
import com.jippy.division.dto.PromotionScheduleDetailsDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.entity.PromotionSchedule;
import com.jippy.division.enums.LocationType;
import com.jippy.division.enums.PromotionScheduleStatus;
import com.jippy.division.enums.PromotionSourceType;
import com.jippy.division.exception.DivInvalidRequestException;
import com.jippy.division.exception.DivResourceNotFoundException;
import com.jippy.division.feignClient.FMFeignClient;
import com.jippy.division.repositary.DivCouponMappingRepository;
import com.jippy.division.repositary.DivPriceDropMappingRepository;
import com.jippy.division.repositary.DivPromotionDateRepository;
import com.jippy.division.repositary.PromotionScheduleRepository;
import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionScheduleServiceImpl implements PromotionScheduleService {


    private final PromotionScheduleRepository promotionScheduleRepository;
    private final FMFeignClient foodMartFeignClient;
    private final DivPriceDropMappingRepository priceDropRepository;
    private final DivCouponMappingRepository couponMappingRepository;
    private final DivPromotionDateRepository promotionDateRepository;

    @Override
    public void createMerchantPromotionSchedule(Integer promotionPlanId) {

        log.info("[PROMOTION-SCHEDULE] Creating schedules | promotionPlanId={}", promotionPlanId);

        MerchantPromotionDetailsDto details = foodMartFeignClient.getMerchantPromotionDetails(promotionPlanId);

        if (details == null || "N".equalsIgnoreCase(details.getIsActive())) {
            log.warn("[PROMOTION-SCHEDULE] Merchant promotion not found or inactive (is_active=N) | promotionPlanId={}", promotionPlanId);
            return;
        }

        if (promotionScheduleRepository.existsBySourceTypeAndSourceIdAndStatusIn(
                PromotionSourceType.MERCHANT_PROMOTION,
                promotionPlanId,
                List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE))) {
            log.info("[PROMOTION-SCHEDULE] Active or pending schedule already exists for merchant promotion | promotionPlanId={}", promotionPlanId);
            return;
        }

        PromotionScheduleDetailsDto dto = foodMartFeignClient.getPromotionScheduleDetails(promotionPlanId);

        if (dto == null) {

            log.error("[PROMOTION-SCHEDULE] Promotion details not found | promotionPlanId={}", promotionPlanId);

            return;
        }


        saveSchedules(dto);
    }

    @Override
    public void updateMerchantPromotionSchedule(Integer promotionPlanId) {

        log.info("[PROMOTION-SCHEDULE] Updating schedules | promotionPlanId={}", promotionPlanId);

        List<PromotionScheduleStatus> activePending = List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE);
        List<PromotionSchedule> existingSchedules = promotionScheduleRepository.findBySourceTypeAndSourceIdAndStatusIn(
                PromotionSourceType.MERCHANT_PROMOTION, promotionPlanId, activePending);

        MerchantPromotionDetailsDto details = foodMartFeignClient.getMerchantPromotionDetails(promotionPlanId);

        if (details == null || "N".equalsIgnoreCase(details.getIsActive())) {
            log.warn("[PROMOTION-SCHEDULE] Merchant promotion not found or inactive (is_active=N). Cancelling active/pending schedules | promotionPlanId={}", promotionPlanId);
            for (PromotionSchedule s : existingSchedules) {
                s.setStatus(PromotionScheduleStatus.CANCELLED);
            }
            if (!existingSchedules.isEmpty()) {
                promotionScheduleRepository.saveAll(existingSchedules);
            }
            return;
        }

        PromotionScheduleDetailsDto dto = foodMartFeignClient.getPromotionScheduleDetails(promotionPlanId);

        if (dto == null || dto.getProductIds() == null || dto.getProductIds().isEmpty()) {
            log.warn("[PROMOTION-SCHEDULE] Promotion details or products not found for promotionPlanId={}. Cancelling existing schedules", promotionPlanId);
            for (PromotionSchedule s : existingSchedules) {
                s.setStatus(PromotionScheduleStatus.CANCELLED);
            }
            if (!existingSchedules.isEmpty()) {
                promotionScheduleRepository.saveAll(existingSchedules);
            }
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(dto.getPlanStartDate(), dto.getPlanStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(dto.getPlanEndDate(), dto.getPlanEndTime());

        Map<Integer, PromotionSchedule> scheduleByProductMap = new HashMap<>();
        for (PromotionSchedule s : existingSchedules) {
            if (s.getProductId() != null) {
                scheduleByProductMap.put(s.getProductId(), s);
            }
        }

        Set<Integer> requestedProductIds = new HashSet<>(dto.getProductIds());
        List<PromotionSchedule> schedulesToSave = new ArrayList<>();

        for (Integer productId : requestedProductIds) {
            PromotionSchedule schedule = scheduleByProductMap.remove(productId);
            if (schedule != null) {
                schedule.setLocationType(LocationType.AREA);
                schedule.setLocationId(dto.getAreaId());
                schedule.setOutletId(dto.getOutletId());
                schedule.setStartDateTime(startDateTime);
                schedule.setEndDateTime(endDateTime);
                schedulesToSave.add(schedule);
            } else {
                PromotionSchedule newSchedule = new PromotionSchedule();
                newSchedule.setSourceType(PromotionSourceType.MERCHANT_PROMOTION);
                newSchedule.setSourceId(dto.getPromotionPlanId());
                newSchedule.setLocationType(LocationType.AREA);
                newSchedule.setLocationId(dto.getAreaId());
                newSchedule.setOutletId(dto.getOutletId());
                newSchedule.setProductId(productId);
                newSchedule.setStartDateTime(startDateTime);
                newSchedule.setEndDateTime(endDateTime);
                newSchedule.setStatus(PromotionScheduleStatus.PENDING);
                schedulesToSave.add(newSchedule);
            }
        }

        for (PromotionSchedule removedSchedule : scheduleByProductMap.values()) {
            removedSchedule.setStatus(PromotionScheduleStatus.CANCELLED);
            schedulesToSave.add(removedSchedule);
        }

        if (!schedulesToSave.isEmpty()) {
            promotionScheduleRepository.saveAll(schedulesToSave);
        }
    }

    @Override
    public void deleteMerchantPromotionSchedule(Integer promotionPlanId) {

        log.info("[PROMOTION-SCHEDULE] Deleting schedules | promotionPlanId={}", promotionPlanId);

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(PromotionSourceType.MERCHANT_PROMOTION, promotionPlanId);
    }

    @Override
    public void createPriceDropSchedule(Integer priceDropMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Creating Price Drop schedule | mappingId={}",
                priceDropMappingId);

        DivPriceDropMappingOutletsProduct mapping =
                priceDropRepository.findById(priceDropMappingId)
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Price Drop Mapping not found with id : "
                                        + priceDropMappingId));

        if ("N".equalsIgnoreCase(mapping.getIsActive())) {
            log.warn("[PROMOTION-SCHEDULE] Price Drop mapping is inactive (is_active=N). Skipping schedule creation | mappingId={}", priceDropMappingId);
            return;
        }

        DivPromotionDate promotionDate =
                promotionDateRepository.findById(
                                mapping.getPromotionDateId())
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Promotion Date not found with id : "
                                        + mapping.getPromotionDateId()));

        PromotionSchedule schedule = new PromotionSchedule();

        schedule.setSourceType(PromotionSourceType.PRICE_DROP);
        schedule.setSourceId(
                mapping.getPriceDropMappingOutletsProductsId());

        schedule.setLocationType(
                LocationType.valueOf(mapping.getLocationType()));

        schedule.setLocationId(
                mapping.getLocationId());

        schedule.setOutletId(
                mapping.getOutletId());

        schedule.setProductId(
                mapping.getProductId());

        schedule.setStartDateTime(
                promotionDate.getPromotionFromDate());

        schedule.setEndDateTime(
                promotionDate.getPromotionToDate());

        schedule.setStatus(PromotionScheduleStatus.PENDING);

        promotionScheduleRepository.save(schedule);

        log.info(
                "[PROMOTION-SCHEDULE] Price Drop schedule created successfully | mappingId={}",
                priceDropMappingId);
    }

    @Override
    public void updatePriceDropSchedule(Integer priceDropMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Updating Price Drop schedule | mappingId={}",
                priceDropMappingId);

        DivPriceDropMappingOutletsProduct mapping = priceDropRepository.findById(priceDropMappingId).orElse(null);
        if (mapping == null || "N".equalsIgnoreCase(mapping.getIsActive())) {
            List<PromotionSchedule> existingSchedules = promotionScheduleRepository.findBySourceTypeAndSourceIdAndStatusIn(
                    PromotionSourceType.PRICE_DROP, priceDropMappingId, List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE));
            for (PromotionSchedule s : existingSchedules) {
                s.setStatus(PromotionScheduleStatus.CANCELLED);
            }
            if (!existingSchedules.isEmpty()) {
                promotionScheduleRepository.saveAll(existingSchedules);
            }
            return;
        }

        DivPromotionDate promotionDate = promotionDateRepository.findById(mapping.getPromotionDateId()).orElse(null);
        if (promotionDate == null) {
            log.error("[PROMOTION-SCHEDULE] Promotion Date not found for priceDropMappingId={}", priceDropMappingId);
            return;
        }

        java.util.Optional<PromotionSchedule> existingScheduleOpt = promotionScheduleRepository.findFirstBySourceTypeAndSourceIdAndStatusIn(
                PromotionSourceType.PRICE_DROP, priceDropMappingId, List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE));

        if (existingScheduleOpt.isPresent()) {
            PromotionSchedule schedule = existingScheduleOpt.get();
            schedule.setLocationType(LocationType.valueOf(mapping.getLocationType()));
            schedule.setLocationId(mapping.getLocationId());
            schedule.setOutletId(mapping.getOutletId());
            schedule.setProductId(mapping.getProductId());
            schedule.setStartDateTime(promotionDate.getPromotionFromDate());
            schedule.setEndDateTime(promotionDate.getPromotionToDate());
            promotionScheduleRepository.save(schedule);
        } else {
            createPriceDropSchedule(priceDropMappingId);
        }
    }

    @Override
    public void deletePriceDropSchedule(Integer priceDropMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Deleting Price Drop schedule | mappingId={}",
                priceDropMappingId);

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(
                PromotionSourceType.PRICE_DROP,
                priceDropMappingId);
    }

    @Override
    public void createCouponSchedule(Integer couponMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Creating Coupon schedule | mappingId={}",
                couponMappingId);

        DivCouponMappingOutletProduct mapping =
                couponMappingRepository.findById(couponMappingId)
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Coupon Mapping not found with id : "
                                        + couponMappingId));

        if ("N".equalsIgnoreCase(mapping.getIsActive())) {
            log.warn("[PROMOTION-SCHEDULE] Coupon mapping is inactive (is_active=N). Skipping schedule creation | mappingId={}", couponMappingId);
            return;
        }

        DivPromotionDate promotionDate =
                promotionDateRepository.findById(
                                mapping.getPromotionDateId())
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Promotion Date not found with id : "
                                        + mapping.getPromotionDateId()));

        PromotionSchedule schedule = new PromotionSchedule();

        schedule.setSourceType(PromotionSourceType.COUPON);
        schedule.setSourceId(
                mapping.getCouponMappingId());

        schedule.setLocationType(
                LocationType.valueOf(mapping.getLocationType()));

        schedule.setLocationId(
                mapping.getLocationId());

        schedule.setOutletId(
                mapping.getOutletId());

        schedule.setProductId(
                mapping.getProductId());

        schedule.setStartDateTime(
                promotionDate.getPromotionFromDate());

        schedule.setEndDateTime(
                promotionDate.getPromotionToDate());

        schedule.setStatus(PromotionScheduleStatus.PENDING);

        promotionScheduleRepository.save(schedule);

        log.info(
                "[PROMOTION-SCHEDULE] Coupon schedule created successfully | mappingId={}",
                couponMappingId);
    }

    @Override
    public void updateCouponSchedule(Integer couponMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Updating Coupon schedule | mappingId={}",
                couponMappingId);

        DivCouponMappingOutletProduct mapping = couponMappingRepository.findById(couponMappingId).orElse(null);
        if (mapping == null || "N".equalsIgnoreCase(mapping.getIsActive())) {
            List<PromotionSchedule> existingSchedules = promotionScheduleRepository.findBySourceTypeAndSourceIdAndStatusIn(
                    PromotionSourceType.COUPON, couponMappingId, List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE));
            for (PromotionSchedule s : existingSchedules) {
                s.setStatus(PromotionScheduleStatus.CANCELLED);
            }
            if (!existingSchedules.isEmpty()) {
                promotionScheduleRepository.saveAll(existingSchedules);
            }
            return;
        }

        DivPromotionDate promotionDate = promotionDateRepository.findById(mapping.getPromotionDateId()).orElse(null);
        if (promotionDate == null) {
            log.error("[PROMOTION-SCHEDULE] Promotion Date not found for couponMappingId={}", couponMappingId);
            return;
        }

        Optional<PromotionSchedule> existingScheduleOpt = promotionScheduleRepository.findFirstBySourceTypeAndSourceIdAndStatusIn(
                PromotionSourceType.COUPON, couponMappingId, List.of(PromotionScheduleStatus.PENDING, PromotionScheduleStatus.ACTIVE));

        if (existingScheduleOpt.isPresent()) {
            PromotionSchedule schedule = existingScheduleOpt.get();
            schedule.setLocationType(LocationType.valueOf(mapping.getLocationType()));
            schedule.setLocationId(mapping.getLocationId());
            schedule.setOutletId(mapping.getOutletId());
            schedule.setProductId(mapping.getProductId());
            schedule.setStartDateTime(promotionDate.getPromotionFromDate());
            schedule.setEndDateTime(promotionDate.getPromotionToDate());
            promotionScheduleRepository.save(schedule);
        } else {
            createCouponSchedule(couponMappingId);
        }
    }

    @Override
    public void deleteCouponSchedule(Integer couponMappingId) {

        log.info(
                "[PROMOTION-SCHEDULE] Deleting Coupon schedule | mappingId={}",
                couponMappingId);

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(
                PromotionSourceType.COUPON,
                couponMappingId);
    }

    private void saveSchedules(PromotionScheduleDetailsDto dto) {

        if (dto.getProductIds() == null || dto.getProductIds().isEmpty()) {

            log.warn("[PROMOTION-SCHEDULE] No products found for promotionPlanId={}", dto.getPromotionPlanId());

            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(dto.getPlanStartDate(), dto.getPlanStartTime());

        LocalDateTime endDateTime = LocalDateTime.of(dto.getPlanEndDate(), dto.getPlanEndTime());

        List<PromotionSchedule> schedules = new ArrayList<>();

        for (Integer productId : dto.getProductIds()) {

            PromotionSchedule schedule = new PromotionSchedule();

            schedule.setSourceType(PromotionSourceType.MERCHANT_PROMOTION);
            schedule.setSourceId(dto.getPromotionPlanId());

            schedule.setLocationType(LocationType.AREA);
            schedule.setLocationId(dto.getAreaId());

            schedule.setOutletId(dto.getOutletId());
            schedule.setProductId(productId);

            schedule.setStartDateTime(startDateTime);
            schedule.setEndDateTime(endDateTime);
            schedule.setStatus(PromotionScheduleStatus.PENDING);
            schedules.add(schedule);
        }

        promotionScheduleRepository.saveAll(schedules);

        log.info("[PROMOTION-SCHEDULE] {} schedules generated | promotionPlanId={}", schedules.size(), dto.getPromotionPlanId());
    }

    @Override
    @Transactional
    public void updateScheduleStatuses() {
        LocalDateTime now = LocalDateTime.now();

        int activated = promotionScheduleRepository.activatePendingSchedules(now);
        int expired = promotionScheduleRepository.expireActiveAndPendingSchedules(now);

        if (activated > 0 || expired > 0) {
            log.info("[PROMOTION-SCHEDULE-SCHEDULER] Status transitions executed | activated={} | expired={}", activated, expired);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelPromotion(PromotionCancelRequestDto request) {

        log.info("[PROMOTION-CANCEL] Processing cancellation | sourceType={} | sourceId={}",
                request.getSourceType(), request.getSourceId());

        if (request.getSourceType() == null || request.getSourceId() == null) {
            throw new DivInvalidRequestException("Source type and source id are required for cancellation.");
        }

        switch (request.getSourceType()) {

            case MERCHANT_PROMOTION -> {
                try {
                    foodMartFeignClient.deactivateMerchantPromotion(request.getSourceId());
                } catch (feign.FeignException.NotFound ex) {
                    throw new DivResourceNotFoundException("Merchant Promotion not found with id : " + request.getSourceId());
                } catch (feign.FeignException ex) {
                    log.error("[PROMOTION-CANCEL] Failed remote deactivation in foodandmart | promotionPlanId={}", request.getSourceId(), ex);
                    throw new DivInvalidRequestException("Failed to deactivate merchant promotion in foodandmart service: " + ex.getMessage());
                }

                int count = promotionScheduleRepository.cancelSchedulesBySourceTypeAndSourceId(
                        PromotionSourceType.MERCHANT_PROMOTION.name(),
                        request.getSourceId());

                log.info("[PROMOTION-CANCEL] Merchant Promotion cancelled | promotionPlanId={} | schedulesCancelled={}",
                        request.getSourceId(), count);
            }

            case PRICE_DROP -> {
                DivPriceDropMappingOutletsProduct mapping = priceDropRepository.findById(request.getSourceId())
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Price Drop Mapping not found with id : " + request.getSourceId()));

                mapping.setIsActive("N");
                priceDropRepository.save(mapping);

                int count = promotionScheduleRepository.cancelSchedulesBySourceTypeAndSourceId(
                        PromotionSourceType.PRICE_DROP.name(),
                        request.getSourceId());

                log.info("[PROMOTION-CANCEL] Price Drop cancelled | mappingId={} | schedulesCancelled={}",
                        request.getSourceId(), count);
            }

            case COUPON -> {
                DivCouponMappingOutletProduct mapping = couponMappingRepository.findById(request.getSourceId())
                        .orElseThrow(() -> new DivResourceNotFoundException(
                                "Coupon Mapping not found with id : " + request.getSourceId()));

                mapping.setIsActive("N");
                couponMappingRepository.save(mapping);

                int count = promotionScheduleRepository.cancelSchedulesBySourceTypeAndSourceId(
                        PromotionSourceType.COUPON.name(),
                        request.getSourceId());

                log.info("[PROMOTION-CANCEL] Coupon cancelled | mappingId={} | schedulesCancelled={}",
                        request.getSourceId(), count);
            }

            default -> throw new DivInvalidRequestException("Unsupported promotion source type for cancellation.");
        }

        return "Promotion cancelled successfully.";
    }
}