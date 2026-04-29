package com.jippy.notification.exception;

import com.jippy.notification.dto.NErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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