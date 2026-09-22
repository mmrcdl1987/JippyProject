package com.jippy.foodandmart.projections;

import java.time.LocalDate;

/**
 * Projection used to retrieve settlement week slot details.
 */
public interface FmSettlementWeekSlotProjection {

    /**
     * Settlement slot start date.
     */
    LocalDate getSlotStartDate();

    /**
     * Settlement slot end date.
     */
    LocalDate getSlotEndDate();

    /**
     * Slot type.
     */
    String getSlotType();
}