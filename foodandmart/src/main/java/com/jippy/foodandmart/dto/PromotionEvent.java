package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.PromotionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionEvent implements Serializable {

    private UUID eventId;

    private PromotionEventType eventType;

    /**
     * promotion_plan_id
     */
    private Integer sourceId;

    private LocalDateTime eventTime;
}