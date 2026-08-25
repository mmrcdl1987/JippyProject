package com.jippy.driver.exception;


import com.jippy.driver.dto.DriverErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── BUSINESS EXCEPTIONS ────────────────────────────────────────────────────


    // Handle CartException specifically
    @ExceptionHandler(CartException.class)
    public ResponseEntity<DriverErrorResponseDto> handleCartException
    (CartException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }


    @ExceptionHandler(DriverBadRequestException.class)
    public ResponseEntity<DriverErrorResponseDto> handleBadRequest(DriverBadRequestException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DriverErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    // GENERIC EXCEPTION


    @ExceptionHandler(DriverZoneException.class)
    public ResponseEntity<DriverErrorResponseDto> handleZoneException(
            DriverZoneException ex,
            HttpServletRequest request) {

        log.error("Zone exception: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Handles Driver Business exceptions.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(DriverBusinessException.class)
    public ResponseEntity<DriverErrorResponseDto> handleBusinessException(
            DriverBusinessException ex,
            HttpServletRequest request) {

        log.error("Business exception: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(ImageValidationException.class)
    public ResponseEntity<DriverErrorResponseDto> handleImageValidationException
            (ImageValidationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Handles validation errors for @Valid request bodies.
     * <p>
     * Returns field-wise validation messages instead of
     * Spring's default exception response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException
    (MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed for request: {}", request.getRequestURI());

        Map<String, String> validationErrors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("apiPath", request.getRequestURI());
        response.put("errorCode", "VALIDATION_FAILED");
        response.put("errorMessage", "Validation failed.");
        response.put("errors", validationErrors);
        response.put("errorTime", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DriverErrorResponseDto> handleHttpMessageNotReadable
            (HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Invalid request body received: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body
                (new DriverErrorResponseDto(request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        "Invalid request data type or malformed JSON.",
                        LocalDateTime.now()));
    }
    /**
     * Handles Resource Not Found exceptions.
     * Returns HTTP 404 when requested resource is not available.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<DriverErrorResponseDto> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
    /**
     * Handles invalid request parameter datatype.
     * Example:
     * GET /getDriverDetails?driverId=abc
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<DriverErrorResponseDto> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.error("Invalid request parameter: {}", ex.getName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        "Invalid value for parameter : " + ex.getName(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception occurred", ex);
        Map<String, Object> error = new HashMap<>();

        error.put("apiPath", request.getRequestURI());
        error.put("errorCode", "INTERNAL_SERVER_ERROR");
        error.put("errorMessage", ex.getMessage());
        error.put("errorTime", LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<DriverErrorResponseDto> handleEmailSendingException(
            EmailSendingException ex,
            HttpServletRequest request) {

        log.error(
                "EMAIL_SENDING_FAILED | path={} | message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DriverErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

}