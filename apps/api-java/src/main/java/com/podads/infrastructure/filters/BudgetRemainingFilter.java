package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class BudgetRemainingFilter implements Filter {
    @Override
    public String getName() {
        return "BudgetRemainingFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        Random rng = new Random(randomSeed);
        
        boolean hasBudget = candidate.getCampaign().getBudget().getRemaining() > 0;
        if (!hasBudget) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.BUDGET_EXHAUSTED)
                    .details("Campaign budget exhausted")
                    .build();
        }
        // Simulate concurrent budget reservations: when budget < $100, 1% chance of exhaustion
        // This models real-world race conditions where multiple requests compete for same budget
        if (rng.nextDouble() < 0.01 && candidate.getCampaign().getBudget().getRemaining() < 10000) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.BUDGET_EXHAUSTED)
                    .details("Budget exhausted due to concurrent reservations")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


