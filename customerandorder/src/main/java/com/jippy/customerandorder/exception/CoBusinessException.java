package com.jippy.customerandorder.exception;

/**
 * Custom Business Exception
 */
public class CoBusinessException extends RuntimeException {

    public CoBusinessException(String message) {
        super(message);
    }
}