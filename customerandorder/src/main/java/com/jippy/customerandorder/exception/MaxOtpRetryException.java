package com.jippy.customerandorder.exception;

public class MaxOtpRetryException extends RuntimeException {

    public MaxOtpRetryException(String message) {
        super(message);
    }
}