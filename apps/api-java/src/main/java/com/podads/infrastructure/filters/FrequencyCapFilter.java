package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.FrequencyCap;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class FrequencyCapFilter implements Filter {
    @Override
    public String getName() {
        return "FrequencyCapFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        Random rng = new Random(randomSeed);
        FrequencyCap freqCap = candidate.getCampaign().getFrequencyCap();
        
        if (freqCap == null) {
            return FilterResult.builder().passed(true).build(); // No frequency cap = always pass
        }
        
        // Simulate frequency cap: 1% chance of rejection (models user hitting impression limit)
        // In production, this would check actual impression history per user
        if (rng.nextDouble() < 0.01) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.FREQUENCY_CAP_EXCEEDED)
                    .details(String.format("Frequency cap exceeded: %d impressions in %dh",
                            freqCap.getMaxImpressions(), freqCap.getWindowHours()))
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}

