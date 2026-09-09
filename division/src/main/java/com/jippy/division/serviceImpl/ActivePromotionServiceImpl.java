package com.jippy.division.serviceImpl;

import com.jippy.division.dto.ActivePromotionDto;
import com.jippy.division.dto.ActivePromotionRequestDto;
import com.jippy.division.dto.DivMealTypeTimingResponseDto;
import com.jippy.division.dto.MerchantPromotionDetailsDto;
import com.jippy.division.entity.PromotionSchedule;
import com.jippy.division.enums.PromotionSourceType;
import com.jippy.division.feignClient.FMFeignClient;
import com.jippy.division.projection.ActiveCouponProjection;
import com.jippy.division.projection.DivActiveDiscountsProjection;
import com.jippy.division.repositary.DivCouponRepository;
import com.jippy.division.repositary.PromotionScheduleRepository;
import com.jippy.division.service.ActivePromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ActivePromotionServiceImpl implements ActivePromotionService {

    private final PromotionScheduleRepository promotionScheduleRepository;
    private final DivCouponRepository couponRepository;
    private final FMFeignClient foodMartFeignClient;

    @Override
    public List<ActivePromotionDto> getActivePromotions(ActivePromotionRequestDto requestDto) {

        log.info("[ACTIVE-PROMOTION] Fetching active promotions | customerId={} | outletId={} | productIds={}", requestDto.getCustomerId(), requestDto.getOutletId(), requestDto.getProductIds());

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        List<ActivePromotionDto> activePromotions = new ArrayList<>();
        // FETCH MAPPED PRICE DROP + MAPPED COUPONS
        List<Integer> productIds = requestDto.getProductIds();

        boolean productIdsProvided = productIds != null && !productIds.isEmpty();

        List<Integer> queryProductIds = productIdsProvided ? productIds : List.of(-1);

        List<DivActiveDiscountsProjection> activeDiscounts = promotionScheduleRepository.getActiveDiscountsByOutletAndProducts(requestDto.getOutletId(), queryProductIds, productIdsProvided, now);
        boolean requiresMealTimingValidation = activeDiscounts.stream().anyMatch(projection -> (PromotionSourceType.PRICE_DROP.name().equals(projection.getSourceType()) || PromotionSourceType.COUPON.name().equals(projection.getSourceType())) && projection.getMealTypeSlotIdsStr() != null && !projection.getMealTypeSlotIdsStr().isBlank());
        List<DivMealTypeTimingResponseDto> mealTypeTimings = requiresMealTimingValidation ? getAllMealTypeTimings() : List.of();

        List<ActivePromotionDto> mappedPromotions = activeDiscounts.stream().filter(projection -> isMealTimingApplicable(projection, mealTypeTimings, currentTime)).map(this::mapToDto).toList();

        activePromotions.addAll(mappedPromotions);

        log.info("[ACTIVE-PROMOTION] Active mapped promotions found | outletId={} | count={}", requestDto.getOutletId(), mappedPromotions.size());

        // FETCH ACTIVE GLOBAL COUPONS
        // NO MEAL TIMING FILTER

        List<ActivePromotionDto> unmappedCoupons = getActiveUnmappedCoupons();

        activePromotions.addAll(unmappedCoupons);

        log.info("[ACTIVE-PROMOTION] Active global coupons found | count={}", unmappedCoupons.size());
        // FETCH ACTIVE MERCHANT PROMOTIONS
        // NO MEAL TIMING FILTER
        List<PromotionSchedule> merchantPromotionSchedules = getActiveMerchantPromotionSchedules(requestDto, now);

        List<ActivePromotionDto> merchantPromotions = getMerchantPromotionDetails(merchantPromotionSchedules);

        activePromotions.addAll(merchantPromotions);

        log.info("""
                [ACTIVE-PROMOTION] Active promotions fetched successfully
                | customerId={}
                | outletId={}
                | mappedCount={}
                | globalCouponCount={}
                | merchantCount={}
                | totalCount={}
                """, requestDto.getCustomerId(), requestDto.getOutletId(), mappedPromotions.size(), unmappedCoupons.size(), merchantPromotions.size(), activePromotions.size());

        return activePromotions;
    }

    // FETCH ALL MEAL TYPE TIMINGS from FOOD MART SERVICE

    private List<DivMealTypeTimingResponseDto> getAllMealTypeTimings() {

        try {

            List<DivMealTypeTimingResponseDto> mealTypeTimings = foodMartFeignClient.getAllMealTypeTimingss();

            if (mealTypeTimings == null) {
                return List.of();
            }

            log.info("[ACTIVE-PROMOTION] Meal type timings fetched | count={}", mealTypeTimings.size());

            return mealTypeTimings;

        } catch (Exception exception) {

            log.error("[ACTIVE-PROMOTION] Failed to fetch meal type timings. Meal-restricted promotions will not be applicable", exception);

            return List.of();
        }
    }

    // MEAL TIMING VALIDATION
// ONLY PRICE_DROP + COUPON
    private boolean isMealTimingApplicable(DivActiveDiscountsProjection projection, List<DivMealTypeTimingResponseDto> mealTypeTimings, LocalTime currentTime) {

        String sourceType = projection.getSourceType();

        boolean requiresMealTimingValidation = PromotionSourceType.PRICE_DROP.name().equals(sourceType) || PromotionSourceType.COUPON.name().equals(sourceType);

        if (!requiresMealTimingValidation) {
            return true;
        }

        String mealTypeSlotIdsStr = projection.getMealTypeSlotIdsStr();

        // No meal timing mapping means promotion is applicable
        if (mealTypeSlotIdsStr == null || mealTypeSlotIdsStr.isBlank()) {
            return true;
        }

        Set<Integer> mealTimingIds = parseMealTimingIds(mealTypeSlotIdsStr);

        if (mealTimingIds.isEmpty()) {

            log.warn("[ACTIVE-PROMOTION] Invalid meal timing configuration | promotionScheduleId={} | mealTypeSlotIds={}", projection.getPromotionScheduleId(), mealTypeSlotIdsStr);

            return false;
        }

        // Meal timings could not be fetched
        if (mealTypeTimings == null || mealTypeTimings.isEmpty()) {

            log.warn("[ACTIVE-PROMOTION] Meal timings unavailable. Promotion will not be applicable | promotionScheduleId={} | mealTimingIds={}", projection.getPromotionScheduleId(), mealTimingIds);

            return false;
        }

        List<DivMealTypeTimingResponseDto> applicableMealTimings = mealTypeTimings.stream().filter(Objects::nonNull).filter(timing -> mealTimingIds.contains(timing.getMealTypeTimingsId())).toList();

        if (applicableMealTimings.isEmpty()) {

            log.warn("[ACTIVE-PROMOTION] Configured meal timing IDs not found | promotionScheduleId={} | mealTimingIds={}", projection.getPromotionScheduleId(), mealTimingIds);

            return false;
        }

        boolean applicable = applicableMealTimings.stream().anyMatch(timing -> {

            LocalTime fromTime = parseLocalTime(timing.getFromTime());
            LocalTime toTime = parseLocalTime(timing.getToTime());

            return isCurrentTimeWithinMealTiming(currentTime, fromTime, toTime);
        });
        if (applicable) {

            log.debug("[ACTIVE-PROMOTION] Promotion applicable for current meal timing | promotionScheduleId={} | sourceType={} | mealTimingIds={} | currentTime={}", projection.getPromotionScheduleId(), projection.getSourceType(), mealTimingIds, currentTime);

        } else {

            log.debug("[ACTIVE-PROMOTION] Promotion not applicable for current meal timing | promotionScheduleId={} | sourceType={} | mealTimingIds={} | currentTime={}", projection.getPromotionScheduleId(), projection.getSourceType(), mealTimingIds, currentTime);
        }

        return applicable;
    }

    // PARSE MEAL TIMING IDS
    // Example: "1,2,3"
    private Set<Integer> parseMealTimingIds(String mealTypeSlotIdsStr) {

        return Arrays.stream(mealTypeSlotIdsStr.split(",")).map(String::trim).filter(value -> !value.isBlank()).map(value -> {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException exception) {

                log.warn("[ACTIVE-PROMOTION] Invalid meal timing ID={}", value);

                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    // CHECK CURRENT TIME AGAINST MEAL TIME
    private boolean isCurrentTimeWithinMealTiming(LocalTime currentTime, LocalTime fromTime, LocalTime toTime) {

        if (currentTime == null || fromTime == null || toTime == null) {
            return false;
        }

        // Invalid configuration
        if (fromTime.equals(toTime)) {
            return false;
        }

        // Normal range: start inclusive, end exclusive
        // Example: 05:00 -> 11:00
        if (fromTime.isBefore(toTime)) {
            return !currentTime.isBefore(fromTime) && currentTime.isBefore(toTime);
        }

        // Overnight range: start inclusive, end exclusive
        // Example: 23:00 -> 02:00
        return !currentTime.isBefore(fromTime) || currentTime.isBefore(toTime);
    }
    // MAPPED PROMOTION DTO
    // PRICE_DROP + MAPPED COUPON

    private ActivePromotionDto mapToDto(DivActiveDiscountsProjection projection) {

        ActivePromotionDto dto = new ActivePromotionDto();

        dto.setProductId(projection.getProductId());
        dto.setPromotionSourceType(projection.getSourceType());
        dto.setSourceId(projection.getSourceId());
        dto.setCouponCode(projection.getCouponCode());
        dto.setDiscountValue(projection.getDiscountAmount());
        dto.setMinimumOrderValue(projection.getMinOrderValue());
        dto.setMaxSelection(projection.getMaxSelection());
        dto.setStartDateTime(projection.getStartDateTime());
        dto.setEndDateTime(projection.getEndDateTime());

        dto.setPromotionName(projection.getPromotionMessage() != null ? projection.getPromotionMessage() : projection.getSourceType());

        dto.setDiscountType(projection.getPriceModelName());

        return dto;
    }

    // GLOBAL COUPONS

    private List<ActivePromotionDto> getActiveUnmappedCoupons() {

        List<ActiveCouponProjection> coupons = couponRepository.findActiveUnmappedCoupons();

        if (coupons == null || coupons.isEmpty()) {
            return List.of();
        }

        return coupons.stream().map(this::mapUnmappedCouponToDto).toList();
    }

    private ActivePromotionDto mapUnmappedCouponToDto(ActiveCouponProjection coupon) {

        ActivePromotionDto dto = new ActivePromotionDto();

        dto.setProductId(null);
        dto.setPromotionSourceType("GLOBAL");
        dto.setSourceId(coupon.getSourceId());
        dto.setPromotionName("Coupon: " + coupon.getCouponCode());
        dto.setCouponCode(coupon.getCouponCode());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setMinimumOrderValue(coupon.getMinimumOrderValue());
        dto.setMaxSelection(-1);
        dto.setStartDateTime(coupon.getStartDateTime());
        dto.setEndDateTime(coupon.getEndDateTime());

        return dto;
    }
    // FETCH ACTIVE MERCHANT PROMOTION SCHEDULES

    private List<PromotionSchedule> getActiveMerchantPromotionSchedules(ActivePromotionRequestDto requestDto, LocalDateTime now) {

        List<Integer> productIds = requestDto.getProductIds();

        if (productIds == null || productIds.isEmpty()) {

            log.info("[ACTIVE-PROMOTION] Product IDs not provided. Skipping merchant promotions | outletId={}", requestDto.getOutletId());

            return List.of();
        }

        List<PromotionSchedule> schedules = promotionScheduleRepository.findActiveMerchantPromotions(PromotionSourceType.MERCHANT_PROMOTION, requestDto.getOutletId(), productIds, now);

        log.info("[ACTIVE-PROMOTION] Active merchant promotion schedules found | outletId={} | count={}", requestDto.getOutletId(), schedules.size());

        return schedules;
    }

    // FETCH MERCHANT PROMOTION DETAILS

    private List<ActivePromotionDto> getMerchantPromotionDetails(List<PromotionSchedule> merchantPromotionSchedules) {

        if (merchantPromotionSchedules == null || merchantPromotionSchedules.isEmpty()) {
            return List.of();
        }

        Set<Integer> promotionPlanIds = merchantPromotionSchedules.stream().map(PromotionSchedule::getSourceId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Integer, MerchantPromotionDetailsDto> promotionDetailsMap = promotionPlanIds.stream().map(this::fetchMerchantPromotionDetails).filter(Objects::nonNull).collect(Collectors.toMap(MerchantPromotionDetailsDto::getPromotionPlanId, details -> details));

        List<ActivePromotionDto> merchantPromotions = new ArrayList<>();

        for (PromotionSchedule schedule : merchantPromotionSchedules) {

            MerchantPromotionDetailsDto promotionDetails = promotionDetailsMap.get(schedule.getSourceId());

            if (promotionDetails == null) {

                log.warn("[ACTIVE-PROMOTION] Merchant promotion details not found | promotionPlanId={} | productId={}", schedule.getSourceId(), schedule.getProductId());

                continue;
            }

            ActivePromotionDto dto = new ActivePromotionDto();

            dto.setProductId(schedule.getProductId());

            dto.setPromotionSourceType(PromotionSourceType.MERCHANT_PROMOTION.name());

            dto.setSourceId(schedule.getSourceId());
            dto.setPromotionName(promotionDetails.getOfferName());
            dto.setDiscountValue(promotionDetails.getOfferAmount());
            dto.setDiscountType(promotionDetails.getOfferType());
            dto.setMinimumOrderValue(promotionDetails.getMinimumOrderValue());
            dto.setMaxSelection(null);
            dto.setStartDateTime(schedule.getStartDateTime());
            dto.setEndDateTime(schedule.getEndDateTime());

            merchantPromotions.add(dto);
        }

        return merchantPromotions;
    }
    // FETCH MERCHANT PROMOTION DETAILS FROM FOODMART SERVICE

    private MerchantPromotionDetailsDto fetchMerchantPromotionDetails(Integer promotionPlanId) {

        try {

            return foodMartFeignClient.getMerchantPromotionDetails(promotionPlanId);

        } catch (Exception exception) {

            log.error("[ACTIVE-PROMOTION] Failed to fetch merchant promotion details | promotionPlanId={}", promotionPlanId, exception);

            return null;
        }
    }

    private LocalTime parseLocalTime(String time) {

        if (time == null || time.isBlank()) {

            log.warn("[ACTIVE-PROMOTION] Meal timing value is null or blank");

            return null;
        }

        try {
            return LocalTime.parse(time);

        } catch (Exception exception) {

            log.warn("[ACTIVE-PROMOTION] Invalid meal timing value={}", time);

            return null;
        }
    }
}