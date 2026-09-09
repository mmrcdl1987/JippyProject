package com.jippy.driver.exception;

public class GoogleRouteException extends RuntimeException {

    public GoogleRouteException(String message) {
        super(message);
    }

    public GoogleRouteException(String message, Throwable cause) {
        super(message, cause);
    }
}