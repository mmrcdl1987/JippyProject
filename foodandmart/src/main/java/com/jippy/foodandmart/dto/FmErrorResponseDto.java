package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(name = "Error Response", description = "Error responses of REST API's")
@Data
@AllArgsConstructor
public class FmErrorResponseDto {

    @Schema(description = "Path of API")
    private String apiPath;

    @Schema(description = "Error Code of API")
    private HttpStatus errorCode;

    @Schema(description = "Error Message of API")
    private String errorMessage;

    @Schema(description = "Time when error occures")
    private LocalDateTime errorTime;


}
