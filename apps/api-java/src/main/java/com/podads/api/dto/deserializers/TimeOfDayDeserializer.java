package com.podads.api.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.podads.domain.valueobjects.TimeOfDay;

import java.io.IOException;

public class TimeOfDayDeserializer extends JsonDeserializer<TimeOfDay> {
    @Override
    public TimeOfDay deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return TimeOfDay.fromString(value);
    }
}
