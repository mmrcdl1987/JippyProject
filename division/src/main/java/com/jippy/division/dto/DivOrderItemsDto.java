package com.jippy.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DivOrderItemsDto {

    @NotNull(message = "Product id is required")
    private Integer productId;

    private Integer variantOptionId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Online unit price is required")
    private BigDecimal onlineUnitPrice;

    @NotNull(message = "Merchant unit price is required")
    private BigDecimal merchantUnitPrice;

    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
