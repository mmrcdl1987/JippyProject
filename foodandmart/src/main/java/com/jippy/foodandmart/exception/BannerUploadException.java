package com.jippy.foodandmart.exception;

public class BannerUploadException extends RuntimeException {

    public BannerUploadException(String message) {
        super(message);
    }

    public BannerUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}