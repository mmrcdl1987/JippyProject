package com.jippy.foodandmart.service;

public interface IFmScheduledPriceService {

    /**
     * Applies price settings whose start date/time has been reached.
     */
    void applyScheduledPrices();

    /**
     * Restores prices for expired price settings.
     */
    void restoreExpiredPrices();
}