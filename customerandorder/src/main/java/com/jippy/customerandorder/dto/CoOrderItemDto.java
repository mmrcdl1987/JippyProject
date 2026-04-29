package com.jippy.customerandorder.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoOrderItemDto {
    @NotNull
    private Integer productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal onlineUnitPrice;

    @NotNull
    private BigDecimal merchantUnitPrice;

}
