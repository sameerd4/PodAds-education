package com.podads.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyCap {
    private Integer maxImpressions;
    private Integer windowHours;
}


