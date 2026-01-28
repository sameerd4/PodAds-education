package com.podads.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterResult {
    private Boolean passed;
    private FilterReasonCode reasonCode;
    private String details;
    private Map<String, Object> metadata;
}


