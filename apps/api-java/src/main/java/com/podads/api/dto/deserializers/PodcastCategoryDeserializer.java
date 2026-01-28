package com.podads.api.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.podads.domain.valueobjects.PodcastCategory;

import java.io.IOException;

public class PodcastCategoryDeserializer extends JsonDeserializer<PodcastCategory> {
    @Override
    public PodcastCategory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return PodcastCategory.fromString(value);
    }
}
