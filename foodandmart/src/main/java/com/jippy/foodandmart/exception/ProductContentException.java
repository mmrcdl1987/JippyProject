package com.jippy.foodandmart.exception;

public class ProductContentException extends RuntimeException {

    public ProductContentException(String message) {
        super(message);
    }

    public ProductContentException(String message, Throwable cause) {
        super(message, cause);
    }
}