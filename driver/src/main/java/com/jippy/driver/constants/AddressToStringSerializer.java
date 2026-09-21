package com.jippy.driver.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jippy.driver.dto.uber.AddressDto;

import java.io.IOException;

public class AddressToStringSerializer extends JsonSerializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (value instanceof AddressDto addressDto) {
            // Write only the human-readable formatted address string to Uber
            if (addressDto.getFormattedAddress() != null) {
                gen.writeString(addressDto.getFormattedAddress());
            } else {
                gen.writeString("");
            }
        } else if (value instanceof String str) {
            gen.writeString(str);
        } else {
            gen.writeString(value.toString());
        }
    }
}
