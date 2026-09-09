package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverFmApiResponse<T> {

    private Boolean success;

    private String message;

    private T data;
}