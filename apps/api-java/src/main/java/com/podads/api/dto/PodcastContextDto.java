package com.podads.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.podads.api.dto.deserializers.PodcastCategoryDeserializer;
import com.podads.domain.valueobjects.PodcastCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PodcastContextDto {
    @NotNull
    @JsonProperty("category")
    @JsonDeserialize(using = PodcastCategoryDeserializer.class)
    private PodcastCategory category;

    @NotBlank
    @JsonProperty("show")
    private String show;

    @NotBlank
    @JsonProperty("episode")
    private String episode;
}


