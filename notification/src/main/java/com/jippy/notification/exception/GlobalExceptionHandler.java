package com.jippy.notification.exception;

import com.jippy.notification.dto.NErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<NErrorResponseDto> handleNotFound(
            NotificationNotFoundException ex,
            HttpServletRequest request) {

        log.warn("NotificationNotFoundException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(DuplicateNotificationException.class)
    public ResponseEntity<NErrorResponseDto> handleDuplicate(
            DuplicateNotificationException ex,
            HttpServletRequest request) {

        log.warn("DuplicateNotificationException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler({
            InvalidNotificationTypeException.class,
            InvalidNotificationRoleException.class,
            InvalidNotificationPriorityException.class
    })
    public ResponseEntity<NErrorResponseDto> handleInvalidEnumExceptions(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Validation Error: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<NErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("MethodArgumentNotValidException: {}", errors);

        return ResponseEntity.badRequest().body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        errors,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<NErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("AccessDeniedException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.FORBIDDEN,
                        "Access denied. ADMIN role required.",
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<NErrorResponseDto> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {

        log.error("NotificationException: {}", ex.getMessage(), ex);

        return ResponseEntity.badRequest().body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<NErrorResponseDto> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled Exception: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new NErrorResponseDto(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        LocalDateTime.now()
                )
        );
    }
}