package com.jippy.customerandorder.exception;

public class OtpAlreadyUsedException extends RuntimeException {
    public OtpAlreadyUsedException(String message) {
        super(message);
    }
}
