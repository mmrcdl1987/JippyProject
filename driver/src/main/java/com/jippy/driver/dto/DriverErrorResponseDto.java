package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class DriverErrorResponseDto {
    private String apiPath;
    private HttpStatus errorCode;
    private String errorMessage;
    private LocalDateTime errorTime;
}
