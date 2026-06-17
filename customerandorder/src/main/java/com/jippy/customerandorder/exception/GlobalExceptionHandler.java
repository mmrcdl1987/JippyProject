package com.jippy.customerandorder.exception;

import com.jippy.customerandorder.dto.CoErrorResponseDto;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<CoErrorResponseDto> handleOrderException(OrderException ex, HttpServletRequest request) {

        log.warn("OrderException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CoBadRequestException.class)
    public ResponseEntity<CoErrorResponseDto> handleBadRequestException(CoBadRequestException ex, HttpServletRequest request) {

        log.warn("CoBadRequestException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CoBusinessException.class)
    public ResponseEntity<CoErrorResponseDto> handleBusinessException(CoBusinessException ex, HttpServletRequest request) {

        log.warn("CoBusinessException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CoOrderSettingsException.class)
    public ResponseEntity<CoErrorResponseDto> handleOrderSettingsException(CoOrderSettingsException ex, HttpServletRequest request) {

        log.error("CoOrderSettingsException | path={} | message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CoReorderException.class)
    public ResponseEntity<CoErrorResponseDto> handleReorderException(CoReorderException ex, HttpServletRequest request) {

        log.warn("CoReorderException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<CoErrorResponseDto> handleCustomerNotFoundException(CustomerNotFoundException ex, HttpServletRequest request) {

        log.warn("CustomerNotFoundException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.NOT_FOUND, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(CoResourceNotFoundException.class)
    public ResponseEntity<CoErrorResponseDto> handleResourceNotFoundException(CoResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("CoResourceNotFoundException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.NOT_FOUND, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler({InvalidOtpException.class, OtpExpiredException.class, OtpNotFoundException.class, OtpAlreadyUsedException.class, MaxOtpRetryException.class, MaxOtpResendException.class, CustomerBlockedException.class})
    public ResponseEntity<CoErrorResponseDto> handleOtpExceptions(RuntimeException ex, HttpServletRequest request) {

        log.warn("OtpException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(SmsFailedException.class)
    public ResponseEntity<CoErrorResponseDto> handleSmsFailedException(SmsFailedException ex, HttpServletRequest request) {

        log.error("SmsFailedException | path={} | message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<CoErrorResponseDto> handleFeignException(FeignException ex, HttpServletRequest request) {

        log.error("FeignException | path={} | status={} | message={}", request.getRequestURI(), ex.status(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE, "Dependent service unavailable", LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CoErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Validation failed");

        log.warn("ValidationException | path={} | message={}", request.getRequestURI(), errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, errorMessage, LocalDateTime.now()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CoErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("ConstraintViolationException | path={} | message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CoErrorResponseDto> handleException(Exception ex, HttpServletRequest request) {

        log.error("UnhandledException | path={} | message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", LocalDateTime.now()));
    }
}