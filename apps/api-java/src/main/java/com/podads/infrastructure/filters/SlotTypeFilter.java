package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

@Component
public class SlotTypeFilter implements Filter {
    @Override
    public String getName() {
        return "SlotTypeFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        if (!candidate.getEligibleSlotTypes().contains(request.getSlot().getType())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.SLOT_TYPE_MISMATCH)
                    .details("Slot type " + request.getSlot().getType() + " not eligible for this creative")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


