package com.jippy.customerandorder.exception;

public class SmsFailedException extends RuntimeException {
    public SmsFailedException(String message) {
        super(message);
    }
}
