package com.jippy.foodandmart.exception;

public class PriceSettingsException extends RuntimeException {

    public PriceSettingsException(String message) {
        super(message);
    }

    public PriceSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}