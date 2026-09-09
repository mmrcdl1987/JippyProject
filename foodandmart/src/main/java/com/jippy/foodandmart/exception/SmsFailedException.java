package com.jippy.foodandmart.exception;

public class SmsFailedException extends RuntimeException {

    public SmsFailedException(String message) {
        super(message);
    }
}