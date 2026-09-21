package com.jippy.customerandorder.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoManifestItemDto {

    private String name;
    private Integer quantity;
    private Double price;

   @JsonProperty("currency_code")
    private String currencyCode;
}
