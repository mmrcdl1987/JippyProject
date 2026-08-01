package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoMealReminderDto {

    /**
     * Customer ID to whom notification should be sent
     */
    private Integer customerId;

    /**
     * Meal Type
     * BREAKFAST
     * LUNCH
     * SNACKS
     * DINNER
     */
    private String mealType;

    private Integer referenceId;
}