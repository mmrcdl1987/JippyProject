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


    //  Generic Exception (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DivErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {

        return new ResponseEntity<>(new DivErrorResponseDto(request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), LocalDateTime.now()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(DivCampaignNotFoundException.class)
    public ResponseEntity<DivErrorResponseDto> handleCampaignNotFound(
            DivCampaignNotFoundException ex,
            WebRequest request) {

        log.error("Campaign not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DivErrorResponseDto(
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
    @ExceptionHandler(DivPromotionDateNotFoundException.class)
    public ResponseEntity<DivErrorResponseDto> handlePromotionDateNotFound(
            DivPromotionDateNotFoundException ex,
            WebRequest request) {

        log.error("Promotion date not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DivErrorResponseDto(
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
    @ExceptionHandler(DivPromotionScheduleException.class)
    public ResponseEntity<DivErrorResponseDto> handlePromotionScheduleException(
            DivPromotionScheduleException ex,
            WebRequest request) {

        log.error("Promotion schedule error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DivErrorResponseDto(
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
    @ExceptionHandler(DivInvalidRequestException.class)
    public ResponseEntity<DivErrorResponseDto> handleInvalidRequest(
            DivInvalidRequestException ex,
            WebRequest request) {

        log.error("Invalid request: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DivErrorResponseDto(
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
}
