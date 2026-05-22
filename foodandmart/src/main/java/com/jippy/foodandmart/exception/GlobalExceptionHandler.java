package com.jippy.foodandmart.exception;

import com.jippy.foodandmart.dto.FmApiResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // --- Validation and Argument Handling ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FmApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        log.warn("Validation failed: {} error(s): {}", errors.size(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FmApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error(ex.getMessage()));
    }

    // --- Resource & Duplicate Handling (Merged) ---

    @ExceptionHandler({ResourceNotFoundException.class, MasterProductNotFoundException.class})
    public ResponseEntity<FmApiResponse<Void>> handleNotFound(Exception ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({MerchantAlreadyExistsException.class, DuplicateResourceException.class, IllegalStateException.class})
    public ResponseEntity<FmApiResponse<Void>> handleConflict(Exception ex) {
        log.warn("Conflict/Duplicate data: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(FmApiResponse.error(ex.getMessage()));
    }

    // --- Specific Business Logic Exceptions ---

    @ExceptionHandler({InvalidUserTypeException.class, PricingException.class})
    public ResponseEntity<FmApiResponse<Void>> handleBusinessLogicErrors(Exception ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error(ex.getMessage()));
    }

    // --- File and System Limits ---

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<FmApiResponse<Void>> handleFileProcessing(FileProcessingException ex) {
        log.error("File processing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FmApiResponse<Void>> handleFileSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(FmApiResponse.error("File size exceeds the maximum allowed limit."));
    }

    // --- Routing and Global Errors ---

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<FmApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        log.debug("Static resource not found (suppressed): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FmApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected server error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FmApiResponse.error("An internal server error occurred. Please try again later."));
    }

    @ExceptionHandler(OutletUnavailabilityException.class)
    public ResponseEntity<FmApiResponse<Void>> handleOutletUnavailabilityException(OutletUnavailabilityException ex) {

        log.warn("Outlet unavailability exception occurred | error={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(OutletUnavailabilitySchedulerException.class)
    public ResponseEntity<FmApiResponse<Void>> handleOutletUnavailabilitySchedulerException(OutletUnavailabilitySchedulerException ex) {

        log.error("Outlet unavailability scheduler exception occurred | error={}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException ex) {

        String message = (ex.status() == 404) ? "No orders found for given customerId" : "Customer & Order service is unavailable";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", message, "timestamp", LocalDateTime.now()));
    }

}
