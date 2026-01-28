package com.podads.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.podads.api.dto.deserializers.DeviceTypeDeserializer;
import com.podads.api.dto.deserializers.TierTypeDeserializer;
import com.podads.api.dto.deserializers.TimeOfDayDeserializer;
import com.podads.domain.valueobjects.DeviceType;
import com.podads.domain.valueobjects.TierType;
import com.podads.domain.valueobjects.TimeOfDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListenerContextDto {
    @NotBlank
    @JsonProperty("geo")
    private String geo;

    @NotNull
    @JsonProperty("device")
    @JsonDeserialize(using = DeviceTypeDeserializer.class)
    private DeviceType device;

    @NotNull
    @JsonProperty("tier")
    @JsonDeserialize(using = TierTypeDeserializer.class)
    private TierType tier;

    @NotNull
    @JsonProperty("consent")
    private Boolean consent;

    @NotNull
    @JsonProperty("timeOfDay")
    @JsonDeserialize(using = TimeOfDayDeserializer.class)
    private TimeOfDay timeOfDay;
}


