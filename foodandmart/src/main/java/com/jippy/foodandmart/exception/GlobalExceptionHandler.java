package com.jippy.foodandmart.exception;

import com.jippy.foodandmart.dto.FmApiResponse;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.List;
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

    @ExceptionHandler({
            MerchantAlreadyExistsException.class,
            DuplicateResourceException.class,
            PromotionPlanTypeAlreadyExistsException.class,
            PromotionPlanAlreadyExistsException.class,
            IllegalStateException.class,
            BadRequestException.class
    })
    public ResponseEntity<FmApiResponse<Void>> handleConflict(Exception ex) {

        log.warn("Conflict/Duplicate data: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(FmApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FmApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FmApiResponse.error("Validation failed", errors));
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
    public ResponseEntity<FmApiResponse<Void>> handleFeignException(FeignException ex) {

        log.error("Feign Exception Status: {}", ex.status());
        log.error("Feign Exception Message: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.resolve(ex.status());

        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        return ResponseEntity.status(status)
                .body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BannerUploadException.class)
    public ResponseEntity<FmApiResponse<Void>> handleBannerUploadException(
            BannerUploadException ex) {

        log.error("Banner upload failed: {}", ex.getMessage(), ex);


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<FmApiResponse<Void>> handleInvalidOtpException(
            InvalidOtpException ex) {

        log.warn("[OTP] Invalid OTP validation failed | reason={}",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<FmApiResponse<Void>> handleOtpExpiredException(
            OtpExpiredException ex) {

        log.warn("[OTP] OTP expired | reason={}",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<FmApiResponse<Void>> handleEmailSendingException(
            EmailSendingException ex) {

        log.error("[EMAIL] Email sending failed | reason={}",
                ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FmApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<FmApiResponse<Void>> handleInvalidTokenException(
            InvalidTokenException ex) {

        log.warn("[TOKEN] Invalid or expired token | reason={}",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(FmApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler({
            InvalidPromotionDateException.class,
            InvalidPromotionAmountException.class,
            InvalidPromotionItemException.class
    })
    public ResponseEntity<FmApiResponse<Void>> handlePromotionValidation(Exception ex) {

        log.warn("[PROMOTION] Validation failed | reason={}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(FmApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(ProductContentException.class)
    public ResponseEntity<FmApiResponse<Void>> handleProductContentException(
            ProductContentException ex) {

        log.error("[PRODUCT CONTENT] {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FmApiResponse.error(ex.getMessage()));
    }

}
