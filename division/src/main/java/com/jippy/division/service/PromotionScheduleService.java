package com.jippy.division.service;

public interface PromotionScheduleService {

    /**
     * Generate schedules for Merchant Promotion.
     */
    void createMerchantPromotionSchedule(Integer promotionPlanId);

    /**
     * Regenerate schedules after Merchant Promotion update.
     */
    void updateMerchantPromotionSchedule(Integer promotionPlanId);

    /**
     * Remove schedules after Merchant Promotion deletion.
     */
    void deleteMerchantPromotionSchedule(Integer promotionPlanId);

    /**
     * Generate schedules for Price Drop.
     */
    void createPriceDropSchedule(Integer priceDropMappingId);

    /**
     * Regenerate schedules after Price Drop update.
     */
    void updatePriceDropSchedule(Integer priceDropMappingId);

    /**
     * Remove schedules after Price Drop deletion.
     */
    void deletePriceDropSchedule(Integer priceDropMappingId);

    /**
     * Generate schedules for Coupon.
     */
    void createCouponSchedule(Integer couponMappingId);

    /**
     * Regenerate schedules after Coupon update.
     */
    void updateCouponSchedule(Integer couponMappingId);

    /**
     * Remove schedules after Coupon deletion.
     */
    void deleteCouponSchedule(Integer couponMappingId);
}