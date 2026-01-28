package com.podads.api.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.podads.domain.valueobjects.TierType;

import java.io.IOException;

public class TierTypeDeserializer extends JsonDeserializer<TierType> {
    @Override
    public TierType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return TierType.fromString(value);
    }
}
