package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TierTargetingFilter implements Filter {
    @Override
    public String getName() {
        return "TierTargetingFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        List<com.podads.domain.valueobjects.TierType> targeting = candidate.getCampaign().getTargeting().getTier();
        if (targeting == null || targeting.isEmpty()) {
            return FilterResult.builder().passed(true).build();
        }
        if (!targeting.contains(request.getListener().getTier())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.TIER_MISMATCH)
                    .details("Listener tier " + request.getListener().getTier() + " not in targeting list")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


