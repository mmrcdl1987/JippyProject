package com.jippy.division.exception;

import com.jippy.division.dto.DivErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class DivGlobalExceptionHandler {

    @ExceptionHandler(DivResourceNotFoundException.class)
    public ResponseEntity<DivErrorResponseDto> handleResourceNotFound(DivResourceNotFoundException ex, WebRequest request) {

        return new ResponseEntity<>(new DivErrorResponseDto(request.getDescription(false), HttpStatus.NOT_FOUND, ex.getMessage(), LocalDateTime.now()), HttpStatus.NOT_FOUND);
    }

    //  Coupon Already Exists
    @ExceptionHandler(DivCouponAlreadyExistsException.class)
    public ResponseEntity<DivErrorResponseDto> handleCouponExists(DivCouponAlreadyExistsException ex, WebRequest request) {

        return new ResponseEntity<>(new DivErrorResponseDto(request.getDescription(false), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DivInvalidDateException.class)
    public ResponseEntity<DivErrorResponseDto> handleInvalidDate(DivInvalidDateException ex, WebRequest request) {

        return new ResponseEntity<>(new DivErrorResponseDto(request.getDescription(false), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ResourceNotFoundException.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {

        log.error("Resource not found exception occurred : {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setTimestamp(LocalDateTime.now());

        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());

        errorResponse.setError(HttpStatus.NOT_FOUND.getReasonPhrase());

        errorResponse.setMessage(ex.getMessage());

        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    //  Generic Exception (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DivErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {

        return new ResponseEntity<>(new DivErrorResponseDto(request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), LocalDateTime.now()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
