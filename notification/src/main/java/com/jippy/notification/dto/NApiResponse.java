package com.jippy.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Common API Response")
public class NApiResponse {

    @Schema(description = "Status of the API", example = "true")
    private boolean success;

    @Schema(description = "Response Message", example = "FCM Token Saved Successfully")
    private String message;

}