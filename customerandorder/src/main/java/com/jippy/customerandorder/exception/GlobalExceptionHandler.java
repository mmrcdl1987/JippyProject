package com.jippy.customerandorder.exception;

import com.jippy.customerandorder.dto.CoErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
}