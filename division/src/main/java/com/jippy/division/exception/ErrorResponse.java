package com.jippy.division.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;
}