package com.jippy.division.service;

import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;
import com.jippy.division.dto.DivPriceModelDto;

import java.util.List;

public interface ICouponService {

    void createCoupon(DivCouponRequestDto couponRequestDto);

    void updateCoupon(DivCouponRequestDto couponRequestDto);

    void enableCoupon(Integer couponId);

    void disableCoupon(Integer couponId);

    DivCouponResponseDto getCouponById(Integer couponId);

    List<DivCouponResponseDto> getAllCoupons(int page, int size);

    List<DivPriceModelDto> getAllPriceModels();

    List<DivCouponResponseDto> getActiveWelcomeCoupons();
}