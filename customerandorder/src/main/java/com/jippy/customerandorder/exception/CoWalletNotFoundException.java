package com.jippy.customerandorder.exception;

public class CoWalletNotFoundException extends RuntimeException {

    public CoWalletNotFoundException(String message) {
        super(message);
    }
}