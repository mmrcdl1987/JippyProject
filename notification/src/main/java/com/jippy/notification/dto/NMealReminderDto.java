package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NMealReminderDto {

    /**
     * Customer Id
     */
    private Integer customerId;

    /**
     * Current Meal Type
     */
    private String mealType;

    private Integer referenceId;

}