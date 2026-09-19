package com.jippy.customerandorder.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AddressToStringSerializer extends JsonSerializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            String jsonString = objectMapper.writeValueAsString(value);
            gen.writeString(jsonString); // Writes it out as a raw JSON String
        } else {
            gen.writeNull();
        }
    }
}
