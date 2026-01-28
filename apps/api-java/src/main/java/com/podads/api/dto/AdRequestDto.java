package com.podads.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.podads.domain.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdRequestDto {
    @NotBlank
    @JsonProperty("requestId")
    private String requestId;

    @NotNull
    @Valid
    @JsonProperty("podcast")
    private PodcastContextDto podcast;

    @NotNull
    @Valid
    @JsonProperty("slot")
    private SlotContextDto slot;

    @NotNull
    @Valid
    @JsonProperty("listener")
    private ListenerContextDto listener;

    @NotBlank
    @JsonProperty("timestamp")
    private String timestamp; // ISO 8601 string
}


