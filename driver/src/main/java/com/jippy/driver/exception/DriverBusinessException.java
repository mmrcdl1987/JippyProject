package com.jippy.driver.exception;

/**
 * Custom Business Exception
 */
public class DriverBusinessException extends RuntimeException {

    public DriverBusinessException(String message) {
        super(message);
    }
}