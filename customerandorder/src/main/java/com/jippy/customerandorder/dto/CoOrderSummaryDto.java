package com.jippy.customerandorder.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoOrderSummaryDto {

    private String orderStatus;
    private String orderId;
    private Integer productId;
    private Integer variantOptionsId;
    private BigDecimal merchantUnitPrice;
    private BigDecimal merchantTotalPrice;
    private Integer quantity;
    private String cookingInstructions;
    private Boolean isCutleryRequired;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderCreatedAt;

}
