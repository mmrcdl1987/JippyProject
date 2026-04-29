package com.jippy.division.serviceImpl;

import com.jippy.division.dto.DivPriceModelDto;
import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;
import com.jippy.division.entity.DivCoupon;
import com.jippy.division.entity.DivPriceModel;
import com.jippy.division.exception.DivCouponAlreadyExistsException;
import com.jippy.division.exception.DivInvalidDateException;
import com.jippy.division.exception.DivResourceNotFoundException;
import com.jippy.division.mapper.DivCouponMapper;
import com.jippy.division.repositary.DivCouponRepository;
import com.jippy.division.repositary.DivPriceModelRepository;
import com.jippy.division.service.ICouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService implements ICouponService {

    @Autowired
    private  DivCouponRepository couponRepository;

    @Autowired
    private DivPriceModelRepository priceModelRepository;

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);


    @Override
    public void createCoupon(DivCouponRequestDto couponRequestDto) {

        log.info("Service createCoupon started for code={}", couponRequestDto.getCouponCode());
        validateCouponNotExists(couponRequestDto.getCouponCode());
        validateDates(couponRequestDto);
        validateDiscount(couponRequestDto);
        DivCoupon coupon = DivCouponMapper.toEntity(new DivCoupon(),couponRequestDto);
        couponRepository.save(coupon);

        log.info("Service createCoupon success for code={}", couponRequestDto.getCouponCode());
    }



    @Override
    public void updateCoupon(DivCouponRequestDto divCouponRequestDto) {

        log.info("Service updateCoupon started id={}", divCouponRequestDto.getCouponId());

        DivCoupon coupon = fetchCouponById(divCouponRequestDto.getCouponId());
        coupon.setUpdatedAt(LocalDateTime.now());
         DivCouponMapper.toEntity(coupon,divCouponRequestDto);
        couponRepository.save(coupon);

        log.info("Service updateCoupon success id={}",divCouponRequestDto.getCouponId());
    }

    @Override
    public void disableCoupon(Integer couponId) {

        log.info("Service disableCoupon started id={}", couponId);

        DivCoupon coupon = fetchCouponById(couponId);

        coupon.setIsActive(false);

        couponRepository.save(coupon);

        log.info("Service disableCoupon success id={}", couponId);
    }

    @Override
    public void enableCoupon(Integer couponId) {

        log.info("Service enableCoupon started id={}", couponId);

        DivCoupon coupon = fetchCouponById(couponId);

        coupon.setIsActive(true);

        couponRepository.save(coupon);

        log.info("Service enableCoupon success id={}", couponId);
    }


    @Override
    public List<DivCouponResponseDto> getAllCoupons(int page, int size) {

        log.info("Service getAllCoupons started page={} size={}", page, size);

        PageRequest pageable = PageRequest.of(page, size);

        List<DivCouponResponseDto> list = couponRepository.findAll(pageable)
                .stream()
                .map(DivCouponMapper::toDTO)
                .toList();

        log.info("Service getAllCoupons success count={}", list.size());

        return list;
    }

    // HELPER METHODS

    private void validateCouponNotExists(String code) {

        couponRepository.findByCouponCode(code)
                .ifPresent(c -> {
                    log.error("Coupon already exists code={}", code);
                    throw new DivCouponAlreadyExistsException("Coupon already exists with code");
                });
    }

    private DivCoupon fetchCouponById(Integer couponId) {

        return couponRepository.findById(couponId)
                .orElseThrow(() -> {
                    log.error("Coupon not found id={}", couponId);
                    return new DivResourceNotFoundException("Coupon not found");
                });
    }
    private void validateDates(DivCouponRequestDto dto) {

        if (dto.getStartDate() != null && dto.getEndDate() != null &&
                dto.getStartDate().isAfter(dto.getEndDate())) {

            log.error("Invalid date range start={} end={}",
                    dto.getStartDate(), dto.getEndDate());

            throw new DivInvalidDateException("Start date cannot be after end date");        }
    }
    private void validateDiscount(DivCouponRequestDto dto) {

        if (dto.getDiscountValue() != null && dto.getMinOrderValue() != null &&
                dto.getDiscountValue() > dto.getMinOrderValue()) {

            throw new IllegalArgumentException("Discount cannot exceed minimum order value");
        }
    }


    @Override
    public List<DivPriceModelDto> getAllPriceModels() {
        List<DivPriceModel> priceModelList = priceModelRepository.findAll();
        List<DivPriceModelDto> priceModelDtoList = List.of();
        if(!priceModelList.isEmpty()){
           priceModelDtoList =  DivCouponMapper.topriceDto(priceModelList);
        }else{
            throw new DivResourceNotFoundException("Price models are not available");
        }
        return  priceModelDtoList;
    }
}
