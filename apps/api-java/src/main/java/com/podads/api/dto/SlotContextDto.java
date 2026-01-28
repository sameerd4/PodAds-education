package com.podads.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.podads.api.dto.deserializers.SlotTypeDeserializer;
import com.podads.domain.valueobjects.SlotType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SlotContextDto {
    @NotNull
    @JsonProperty("type")
    @JsonDeserialize(using = SlotTypeDeserializer.class)
    private SlotType type;

    @JsonProperty("cuePoint")
    private Integer cuePoint;
}


