package com.jippy.customerandorder.exception;

import com.jippy.customerandorder.dto.CoErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    // BUSINESS EXCEPTION
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<CoErrorResponseDto> handleOrderException(
            OrderException ex,
            HttpServletRequest request) {

        log.error("OrderException | path={} | message={}",
                request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CoErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }
    // Handle custom bad request exceptions
    @ExceptionHandler(CoBadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(CoBadRequestException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    // Handle resource not found
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // GENERIC EXCEPTION
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CoErrorResponseDto> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled Exception | path={}", request.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CoErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal server error",
                        LocalDateTime.now()
                ));
    }
//    // Handle all other exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
//
//        // Print full error in console imp for debugging
//        ex.printStackTrace();
//
//        Map<String, String> error = new HashMap<>();
//
//        // Return actual error message instead of generic message
//        error.put("message", ex.getMessage());
//
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

}