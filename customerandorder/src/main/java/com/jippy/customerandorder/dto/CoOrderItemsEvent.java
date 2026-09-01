package com.jippy.customerandorder.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoOrderItemsEvent {

    private  Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private List<CoOrderItemsEvent.VariantDto> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        private Integer productVariantOptionsId;
        private String variantName;
        private BigDecimal variantPrice;
        private String priceType;
    }

}
