package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoFmApiResponse<T> {

    private Boolean success;

    private String message;

    private T data;

    private String timestamp;
}