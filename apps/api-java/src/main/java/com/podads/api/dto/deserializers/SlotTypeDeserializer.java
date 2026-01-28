package com.podads.api.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.podads.domain.valueobjects.SlotType;

import java.io.IOException;

public class SlotTypeDeserializer extends JsonDeserializer<SlotType> {
    @Override
    public SlotType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        
        // Normalize the value: accept both "midroll" and "mid-roll" formats
        // Map common variations to the canonical format
        String normalized = value.toLowerCase().trim();
        if (normalized.equals("midroll") || normalized.equals("mid_roll")) {
            normalized = "mid-roll";
        } else if (normalized.equals("preroll") || normalized.equals("pre_roll")) {
            normalized = "pre-roll";
        } else if (normalized.equals("postroll") || normalized.equals("post_roll")) {
            normalized = "post-roll";
        }
        
        return SlotType.fromString(normalized);
    }
}
