package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.Pacing;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PacingGateFilter implements Filter {
    @Override
    public String getName() {
        return "PacingGateFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        Random rng = new Random(randomSeed);
        Pacing pacing = candidate.getCampaign().getPacing();
        
        if (pacing.getDailyBudget() == null) {
            return FilterResult.builder().passed(true).build(); // No pacing limit
        }
        
        double spendRatio = (pacing.getDailySpend() != null ? pacing.getDailySpend() : 0.0) 
                / (double) pacing.getDailyBudget();
        
        if (spendRatio >= 1.0) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.PACING_LIMIT_EXCEEDED)
                    .details("Daily pacing limit exceeded")
                    .build();
        }
        // Probabilistic throttling: when spend > 90%, 10% chance of rejection
        // This simulates gradual throttling as we approach daily budget limit
        if (spendRatio > 0.9 && rng.nextDouble() < 0.1) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.PACING_LIMIT_EXCEEDED)
                    .details("Pacing throttled to stay within daily budget")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}

