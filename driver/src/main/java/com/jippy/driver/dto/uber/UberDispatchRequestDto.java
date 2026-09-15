package com.jippy.driver.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jippy.driver.constants.AddressToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UberDispatchRequestDto {

    @JsonProperty("external_order_id")
    private String externalOrderId;

    @JsonProperty("pickup_name")
    private String pickupName;

    @JsonSerialize(using = AddressToStringSerializer.class)
    @JsonProperty("pickup_address")
    private AddressDto pickupAddress;

    @JsonProperty("pickup_phone_number")
    private String pickupPhoneNumber;

    @JsonProperty("dropoff_name")
    private String dropOffName;

    @JsonSerialize(using = AddressToStringSerializer.class)
    @JsonProperty("dropoff_address")
    private AddressDto dropOffAddress;

    @JsonProperty("dropoff_phone_number")
    private String dropOffPhoneNumber;

    @JsonProperty("manifest_items")
    private List<ManifestItemDto> manifestItems;
}
