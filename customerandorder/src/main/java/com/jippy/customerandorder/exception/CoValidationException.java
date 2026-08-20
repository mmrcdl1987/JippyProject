package com.jippy.customerandorder.exception;

/**
 * Exception used when a business validation fails.
 */
public class CoValidationException extends RuntimeException {

    public CoValidationException(String message) {
        super(message);
    }
}