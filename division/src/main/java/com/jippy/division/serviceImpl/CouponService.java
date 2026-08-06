package com.jippy.division.serviceImpl;

import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;
import com.jippy.division.dto.DivPriceModelDto;
import com.jippy.division.entity.DivCoupon;
import com.jippy.division.entity.DivPriceModel;
import com.jippy.division.exception.DivCouponAlreadyExistsException;
import com.jippy.division.exception.DivInvalidDateException;
import com.jippy.division.exception.DivResourceNotFoundException;
import com.jippy.division.mapper.DivCouponMapper;
import com.jippy.division.repositary.DivCouponRepository;
import com.jippy.division.repositary.DivPriceModelRepository;
import com.jippy.division.service.ICouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService implements ICouponService {

    private final DivCouponRepository couponRepository;

    private final DivPriceModelRepository priceModelRepository;

    /**
     * Creates a new coupon.
     */
    @Override
    public void createCoupon(DivCouponRequestDto requestDto) {

        log.info("Create coupon request received. CouponCode={}", requestDto.getCouponCode());

        validateCouponNotExists(requestDto.getCouponCode());

        validateDates(requestDto);

        validateDiscount(requestDto);

        validatePriceModel(requestDto.getPriceModelId());

        DivCoupon coupon = DivCouponMapper.toEntity(new DivCoupon(), requestDto);

        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setCreatedBy(requestDto.getCreatedBy());
        coupon.setIsActive(Boolean.TRUE);

        couponRepository.save(coupon);

        log.info("Coupon created successfully. CouponId={}, CouponCode={}",
                coupon.getCouponId(),
                coupon.getCouponCode());
    }

    /**
     * Updates an existing coupon.
     */
    @Override
    public void updateCoupon(DivCouponRequestDto requestDto) {

        log.info("Update coupon request received. CouponId={}", requestDto.getCouponId());

        DivCoupon coupon = fetchCouponById(requestDto.getCouponId());

        if (requestDto.getCouponCode() != null
                && !coupon.getCouponCode().equalsIgnoreCase(requestDto.getCouponCode())) {

            validateCouponNotExists(requestDto.getCouponCode());
        }

        validateDates(requestDto);

        validateDiscount(requestDto);

        validatePriceModel(requestDto.getPriceModelId());

        DivCouponMapper.toEntity(coupon, requestDto);

        coupon.setUpdatedAt(LocalDateTime.now());
        coupon.setUpdatedBy(requestDto.getUpdatedBy());

        couponRepository.save(coupon);

        log.info("Coupon updated successfully. CouponId={}", coupon.getCouponId());
    }

    /**
     * Enables a coupon.
     */
    @Override
    public void enableCoupon(Integer couponId) {

        log.info("Enable coupon request received. CouponId={}", couponId);

        DivCoupon coupon = fetchCouponById(couponId);

        if (Boolean.TRUE.equals(coupon.getIsActive())) {

            log.warn("Coupon is already active. CouponId={}", couponId);

            return;
        }

        coupon.setIsActive(Boolean.TRUE);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);

        log.info("Coupon enabled successfully. CouponId={}", couponId);
    }

    /**
     * Disables a coupon.
     */
    @Override
    public void disableCoupon(Integer couponId) {

        log.info("Disable coupon request received. CouponId={}", couponId);

        DivCoupon coupon = fetchCouponById(couponId);

        if (Boolean.FALSE.equals(coupon.getIsActive())) {

            log.warn("Coupon is already disabled. CouponId={}", couponId);

            return;
        }

        coupon.setIsActive(Boolean.FALSE);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);

        log.info("Coupon disabled successfully. CouponId={}", couponId);
    }

    /**
     * Fetch coupon by id.
     */
    @Override
    public DivCouponResponseDto getCouponById(Integer couponId) {

        log.info("Fetch coupon request received. CouponId={}", couponId);

        DivCoupon coupon = fetchCouponById(couponId);

        log.info("Coupon fetched successfully. CouponId={}", couponId);

        return DivCouponMapper.toDto(coupon);
    }

    /**
     * Fetch all coupons.
     */
    @Override
    public List<DivCouponResponseDto> getAllCoupons(int page, int size) {

        log.info("Fetch all coupons request received. Page={}, Size={}", page, size);

        PageRequest pageable = PageRequest.of(page, size);

        List<DivCouponResponseDto> response = couponRepository.findAll(pageable)
                .stream()
                .map(DivCouponMapper::toDto)
                .toList();

        log.info("Successfully fetched {} coupons.", response.size());

        return response;
    }

    /**
     * Fetch all active coupons for UI dropdown.
     */
    @Override
    public List<DivCouponResponseDto> getAllActiveCoupons() {

        log.info("Fetching all active coupons for dropdown display.");

        List<DivCoupon> activeCoupons = couponRepository.findByIsActiveTrue();

        if (activeCoupons.isEmpty()) {

            log.warn("No active coupons found.");

            return List.of();
        }

        log.info("Successfully fetched {} active coupons.", activeCoupons.size());

        return DivCouponMapper.toDtoList(activeCoupons);
    }

    @Override
    public List<DivCouponResponseDto> getActiveWelcomeCoupons() {

        log.info("Fetching active welcome coupons.");

        List<DivCoupon> coupons = couponRepository.findActiveWelcomeCoupons();

        if (coupons.isEmpty()) {

            log.warn("No active welcome coupons found.");

            return List.of();
        }

        log.info("Fetched {} active welcome coupons.", coupons.size());

        return DivCouponMapper.toDtoList(coupons);
    }

    /**
     * Fetch all price models.
     */
    @Override
    public List<DivPriceModelDto> getAllPriceModels() {

        log.info("Fetch all price models request received.");

        List<DivPriceModel> priceModels = priceModelRepository.findAll();

        if (priceModels.isEmpty()) {

            log.error("No price models found.");

            throw new DivResourceNotFoundException("Price models are not available.");
        }

        log.info("Successfully fetched {} price models.", priceModels.size());

        return DivCouponMapper.toPriceModelDto(priceModels);
    }

    /**
     * Validates that the coupon code does not already exist.
     */
    private void validateCouponNotExists(String couponCode) {

        if (couponRepository.existsByCouponCode(couponCode)) {

            log.error("Coupon already exists. CouponCode={}", couponCode);

            throw new DivCouponAlreadyExistsException(
                    "Coupon already exists with code : " + couponCode);
        }
    }

    /**
     * Fetch coupon by id.
     */
    private DivCoupon fetchCouponById(Integer couponId) {

        return couponRepository.findById(couponId)
                .orElseThrow(() -> {

                    log.error("Coupon not found. CouponId={}", couponId);

                    return new DivResourceNotFoundException(
                            "Coupon not found with id : " + couponId);
                });
    }

    /**
     * Validate start and end dates.
     */
    private void validateDates(DivCouponRequestDto requestDto) {

        if (requestDto.getStartDate() == null || requestDto.getEndDate() == null) {
            return;
        }

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {

            log.error("Invalid coupon dates. StartDate={}, EndDate={}",
                    requestDto.getStartDate(),
                    requestDto.getEndDate());

            throw new DivInvalidDateException(
                    "Start date cannot be after end date.");
        }
    }

    /**
     * Validate discount value.
     */
    private void validateDiscount(DivCouponRequestDto requestDto) {

        if (requestDto.getDiscountValue() == null ||
                requestDto.getMinOrderValue() == null) {
            return;
        }

        if (requestDto.getDiscountValue()
                .compareTo(requestDto.getMinOrderValue()) > 0) {

            log.error(
                    "Invalid discount. DiscountValue={}, MinOrderValue={}",
                    requestDto.getDiscountValue(),
                    requestDto.getMinOrderValue());

            throw new DivInvalidDateException(
                    "Discount value cannot be greater than minimum order value.");
        }
    }

    /**
     * Validate price model.
     */
    private void validatePriceModel(Integer priceModelId) {

        if (priceModelId == null) {
            return;
        }

        if (!priceModelRepository.existsById(priceModelId)) {

            log.error("Invalid PriceModelId={}", priceModelId);

            throw new DivResourceNotFoundException(
                    "Price model not found with id : " + priceModelId);
        }
    }
}