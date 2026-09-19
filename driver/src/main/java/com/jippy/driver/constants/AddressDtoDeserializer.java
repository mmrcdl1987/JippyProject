package com.jippy.driver.constants;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.driver.dto.uber.AddressDto;
import com.jippy.driver.dto.uber.LocationDto;

import java.io.IOException;

public class AddressDtoDeserializer extends JsonDeserializer<AddressDto> {

    @Override
    public AddressDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        AddressDto addressDto = new AddressDto();

        // Handle string or object payloads safely
        if (node.isTextual()) {
            addressDto.setFormattedAddress(node.asText());
        } else if (node.isObject()) {
            if (node.has("formatted_address")) {
                addressDto.setFormattedAddress(node.get("formatted_address").asText());
            }
            if (node.has("location")) {
                JsonNode locNode = node.get("location");
                LocationDto locationDto = new LocationDto();
                if (locNode.has("latitude")) {
                    locationDto.setLatitude(locNode.get("latitude").asDouble());
                }
                if (locNode.has("longitude")) {
                    locationDto.setLongitude(locNode.get("longitude").asDouble());
                }
                addressDto.setLocation(locationDto);
            }
        }

        return addressDto;
    }
}
