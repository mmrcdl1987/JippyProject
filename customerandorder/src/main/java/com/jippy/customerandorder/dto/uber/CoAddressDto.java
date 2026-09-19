package com.jippy.customerandorder.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoAddressDto {

    @JsonProperty("formatted_address")
    private String formattedAddress;

   @JsonProperty("location")
    private CoLocationDto location;
}
