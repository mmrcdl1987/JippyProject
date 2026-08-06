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


    void deletePriceDropSchedule(Integer priceDropMappingId);

    void createCouponSchedule(Integer couponMappingId);

    void updateCouponSchedule(Integer couponMappingId);

    void deleteCouponSchedule(Integer couponMappingId);
}