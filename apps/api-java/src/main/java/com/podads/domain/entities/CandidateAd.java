package com.podads.domain.entities;

import com.podads.domain.valueobjects.SlotType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAd {
    private Campaign campaign;
    private Creative creative;
    private List<SlotType> eligibleSlotTypes;
}


