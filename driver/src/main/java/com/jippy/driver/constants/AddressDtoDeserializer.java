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

        if (node == null) {
            return addressDto;
        }

        // 1. If node is a raw text string, check whether it is a stringified JSON object
        if (node.isTextual()) {
            String text = node.asText();
            if (text.startsWith("{")) {
                try {
                    ObjectMapper mapper = (ObjectMapper) p.getCodec();
                    JsonNode innerNode = mapper.readTree(text);
                    parseJsonNodeIntoDto(innerNode, addressDto);
                    return addressDto;
                } catch (Exception ignored) {
                    // If parsing fails, treat it as a pure string address
                }
            }
            addressDto.setFormattedAddress(text);
        } else if (node.isObject()) {
            // 2. Standard JSON Object processing
            parseJsonNodeIntoDto(node, addressDto);
        }

        return addressDto;
    }

    private void parseJsonNodeIntoDto(JsonNode node, AddressDto addressDto) {
        if (node.has("formatted_address")) {
            addressDto.setFormattedAddress(node.get("formatted_address").asText());
        }

        if (node.has("location") && !node.get("location").isNull()) {
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

}
