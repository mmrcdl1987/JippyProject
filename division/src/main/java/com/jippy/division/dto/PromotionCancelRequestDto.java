package com.jippy.division.dto;

import com.jippy.division.enums.PromotionSourceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionCancelRequestDto {

    @NotNull(message = "Source type is required")
    private PromotionSourceType sourceType;

    @NotNull(message = "Source id is required")
    private Integer sourceId;
}
