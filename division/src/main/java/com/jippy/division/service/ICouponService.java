package com.jippy.division.service;

import com.jippy.division.dto.DivPriceModelDto;
import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;

import java.util.List;

public interface ICouponService {

    void createCoupon(DivCouponRequestDto couponRequestDto);

    void updateCoupon(DivCouponRequestDto divCouponRequestDto);

    void disableCoupon(Integer couponId);

    void enableCoupon(Integer couponId);

    // List<DivCouponResponseDto> getAllCoupons();

    List<DivCouponResponseDto> getAllCoupons(int page, int size);

    List<DivPriceModelDto> getAllPriceModels();
}
