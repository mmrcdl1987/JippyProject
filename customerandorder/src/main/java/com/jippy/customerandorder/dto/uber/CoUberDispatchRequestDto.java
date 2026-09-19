package com.jippy.customerandorder.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jippy.customerandorder.constants.AddressToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoUberDispatchRequestDto {

    @JsonProperty("external_order_id")
    private String externalOrderId;

    @JsonProperty("pickup_name")
    private String pickupName;

    @JsonSerialize(using = AddressToStringSerializer.class)
    @JsonProperty("pickup_address")
    private CoAddressDto pickupAddress;

    @JsonProperty("pickup_phone_number")
    private String pickupPhoneNumber;

    @JsonProperty("dropoff_name")
    private String dropOffName;

    @JsonSerialize(using = AddressToStringSerializer.class)
    @JsonProperty("dropoff_address")
    private CoAddressDto dropOffAddress;

    @JsonProperty("dropoff_phone_number")
    private String dropOffPhoneNumber;

    @JsonProperty("manifest_items")
    private List<CoManifestItemDto> manifestItems;
}
