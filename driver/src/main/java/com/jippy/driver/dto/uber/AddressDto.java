package com.jippy.driver.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.jippy.driver.constants.AddressDtoDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = AddressDtoDeserializer.class)
public class AddressDto {

    @JsonProperty("formatted_address")
    private String formattedAddress;

   @JsonProperty("location")
    private LocationDto location;
}
