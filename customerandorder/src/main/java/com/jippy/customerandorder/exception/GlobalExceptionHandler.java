package com.jippy.customerandorder.exception;

import com.jippy.customerandorder.dto.CoErrorResponseDto;
import feign.FeignException;
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

    // ── BUSINESS EXCEPTIONS ────────────────────────────────────────────────────

    // Handle OrderException (parent of CartException)
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<CoErrorResponseDto> handleOrderException(OrderException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    // Handle CartException specifically
    @ExceptionHandler(CartException.class)
    public ResponseEntity<CoErrorResponseDto> handleCartException(CartException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }


    @ExceptionHandler(CoBadRequestException.class)
    public ResponseEntity<CoErrorResponseDto> handleBadRequest(CoBadRequestException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

    // GENERIC EXCEPTION
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {

        ex.printStackTrace();

        Map<String, Object> error = new HashMap<>();

        error.put("apiPath", request.getRequestURI());
        error.put("errorCode", "INTERNAL_SERVER_ERROR");
        error.put("errorMessage", ex.getMessage());
        error.put("errorTime", LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
/*    @ExceptionHandler(CoZoneException.class)
    public ResponseEntity<Map<String, String>> handleZoneException(CoZoneException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }*/

    @ExceptionHandler(CoBusinessException.class)
    public ResponseEntity<?> handleBusinessException(CoBusinessException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(CoOrderSettingsException.class)
    public ResponseEntity<CoErrorResponseDto>
    handleOrderSettingsException(CoOrderSettingsException ex, HttpServletRequest request) {

        log.error("CoOrderSettingsException | path={} | message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CoErrorResponseDto(request.getRequestURI(), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now()));
    }

//    Handle FM feign exceptions automatically caught by service with out try-catch
//    we can check by making down Fm or ay Microservice and then calling any API
//    which is calling FM service,then it will throw FeignException
//    and we can check the response from this handler
    @ExceptionHandler(FeignException.class)
    public CoErrorResponse handleFeignException(FeignException ex) {

        log.error("Feign exception occurred : {}", ex.getMessage());

//        created inside exception package
        CoErrorResponse error = new CoErrorResponse();

        error.setErrorCode("FM_SERVICE_ERROR");

        error.setErrorMessage("Failed to communicate with FM service");

        return error;
    }

    @ExceptionHandler(CoReorderException.class)
    public ResponseEntity<CoErrorResponseDto> handleReorderException(
            CoReorderException ex,
            HttpServletRequest request) {

        log.error(
                "CoReorderException | path={} | message={}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new CoErrorResponseDto(
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }

}