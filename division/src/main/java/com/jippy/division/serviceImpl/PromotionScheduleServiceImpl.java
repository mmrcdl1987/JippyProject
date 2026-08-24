package com.jippy.division.serviceImpl;

import com.jippy.division.dto.PromotionScheduleDetailsDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.entity.PromotionSchedule;
import com.jippy.division.enums.LocationType;
import com.jippy.division.enums.PromotionSourceType;
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
import java.util.ArrayList;
import java.util.List;

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

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(PromotionSourceType.MERCHANT_PROMOTION, promotionPlanId);

        PromotionScheduleDetailsDto dto = foodMartFeignClient.getPromotionScheduleDetails(promotionPlanId);

        saveSchedules(dto);
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

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(
                PromotionSourceType.PRICE_DROP,
                priceDropMappingId);

        createPriceDropSchedule(priceDropMappingId);
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

        promotionScheduleRepository.deleteBySourceTypeAndSourceId(
                PromotionSourceType.COUPON,
                couponMappingId);

        createCouponSchedule(couponMappingId);
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
            schedules.add(schedule);
        }

        promotionScheduleRepository.saveAll(schedules);

        log.info("[PROMOTION-SCHEDULE] {} schedules generated | promotionPlanId={}", schedules.size(), dto.getPromotionPlanId());
    }
}