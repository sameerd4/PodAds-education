package com.podads.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotContext {
    private SlotType type;
    private Integer cuePoint;
}


