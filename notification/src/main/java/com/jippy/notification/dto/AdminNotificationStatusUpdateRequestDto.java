package com.jippy.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNotificationStatusUpdateRequestDto {

    @NotNull(message = "Active status is required")
    private Boolean active;
}