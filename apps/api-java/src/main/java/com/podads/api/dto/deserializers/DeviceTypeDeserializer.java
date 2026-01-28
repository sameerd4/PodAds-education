package com.podads.api.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.podads.domain.valueobjects.DeviceType;

import java.io.IOException;

public class DeviceTypeDeserializer extends JsonDeserializer<DeviceType> {
    @Override
    public DeviceType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return DeviceType.fromString(value);
    }
}
