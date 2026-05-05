package com.jippy.foodandmart.exception;

public class MasterProductNotFoundException extends RuntimeException {
    public MasterProductNotFoundException(Integer id) {
        super("Master product not found with ID: " + id);
    }
}
