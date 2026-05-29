package com.jippy.customerandorder.exception;

import lombok.Data;

@Data
public class CoErrorResponse {

    private String errorCode;

    private String errorMessage;
}