package com.jippy.foodandmart.util;

import com.jippy.foodandmart.entity.PromotionPlan;
import com.jippy.foodandmart.enums.PromotionStatus;

import java.time.LocalDateTime;

public final class PromotionStatusUtil {

    private PromotionStatusUtil() {
    }

    /**
     * Returns the promotion status based on current date and time.
     */
    public static PromotionStatus getStatus(PromotionPlan promotionPlan) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDateTime = LocalDateTime.of(
                promotionPlan.getPlanStartDate(),
                promotionPlan.getPlanStartTime());

        LocalDateTime endDateTime = LocalDateTime.of(
                promotionPlan.getPlanEndDate(),
                promotionPlan.getPlanEndTime());

        if (now.isBefore(startDateTime)) {
            return PromotionStatus.SCHEDULED;
        }

        if (now.isAfter(endDateTime)) {
            return PromotionStatus.ENDED;
        }

        return PromotionStatus.ACTIVE;
    }

}