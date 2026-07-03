package com.jippy.division.dto;

import lombok.Data;

@Data
public class DivFmApiResponse<T> {

    private Boolean success;

    private String message;

    private T data;
}