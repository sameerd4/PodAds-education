package com.podads.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListenerContext {
    private String geo;
    private DeviceType device;
    private TierType tier;
    private Boolean consent;
    private TimeOfDay timeOfDay;
}


