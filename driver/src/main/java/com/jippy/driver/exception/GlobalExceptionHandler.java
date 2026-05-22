package com.jippy.driver.exception;


import com.jippy.driver.dto.DriverErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── BUSINESS EXCEPTIONS ────────────────────────────────────────────────────



    // Handle CartException specifically
    @ExceptionHandler(CartException.class)
    public ResponseEntity<DriverErrorResponseDto> handleCartException(
            CartException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }


    @ExceptionHandler(DriverBadRequestException.class)
    public ResponseEntity<DriverErrorResponseDto> handleBadRequest(
            DriverBadRequestException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    // GENERIC EXCEPTION
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        ex.printStackTrace();

        Map<String, Object> error = new HashMap<>();

        error.put("apiPath", request.getRequestURI());
        error.put("errorCode", "INTERNAL_SERVER_ERROR");
        error.put("errorMessage", ex.getMessage());
        error.put("errorTime", LocalDateTime.now());

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler(DriverZoneException.class)
    public ResponseEntity<Map<String, String>> handleZoneException(DriverZoneException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DriverBusinessException.class)
    public ResponseEntity<?> handleBusinessException(DriverBusinessException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }



}