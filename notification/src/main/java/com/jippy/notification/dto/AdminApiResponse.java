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
@Schema(description = "Standard API Response Envelope")
public class AdminApiResponse<T> {

    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Descriptive response message", example = "Notification created successfully")
    private String message;

    @Schema(description = "Payload data")
    private T data;

    public static <T> AdminApiResponse<T> success(String message, T data) {
        return new AdminApiResponse<>(true, message, data);
    }

    public static <T> AdminApiResponse<T> success(String message) {
        return new AdminApiResponse<>(true, message, null);
    }

    public static <T> AdminApiResponse<T> error(String message) {
        return new AdminApiResponse<>(false, message, null);
    }
}
