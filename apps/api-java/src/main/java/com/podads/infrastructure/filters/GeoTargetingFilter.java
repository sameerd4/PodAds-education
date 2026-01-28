package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeoTargetingFilter implements Filter {
    @Override
    public String getName() {
        return "GeoTargetingFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        List<String> targeting = candidate.getCampaign().getTargeting().getGeo();
        if (targeting == null || targeting.isEmpty()) {
            return FilterResult.builder().passed(true).build(); // No geo targeting = allow all
        }
        if (!targeting.contains(request.getListener().getGeo())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.GEO_MISMATCH)
                    .details("Listener geo " + request.getListener().getGeo() + " not in targeting list")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


