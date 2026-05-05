package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "Response", description = "Response of REST API's")
@Data
@AllArgsConstructor
public class FmResponseDto {

    @Schema(description = "Status code of API", example = "200")
    private String statusCode;

    @Schema(description = "Status message of REST API", example = "Request Processed Successfully")
    private String statusMsg;


}
