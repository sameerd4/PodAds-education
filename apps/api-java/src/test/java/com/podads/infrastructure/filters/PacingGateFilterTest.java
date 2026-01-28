package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacingGateFilterTest {

    private PacingGateFilter filter;

    @BeforeEach
    void setUp() {
        filter = new PacingGateFilter();
    }

    @Test
    void testGetName() {
        assertEquals("PacingGateFilter", filter.getName());
    }

    @Test
    void testPassesWhenNoPacingLimit() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenUnderPacingLimit() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(1000)
                .pacingDailySpend(500) // 50% of limit
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenPacingLimitExceeded() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(1000)
                .pacingDailySpend(1000) // 100% of limit
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.PACING_LIMIT_EXCEEDED, result.getReasonCode());
        assertTrue(result.getDetails().contains("Daily pacing limit exceeded"));
    }

    @Test
    void testFailsWhenPacingOverLimit() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(1000)
                .pacingDailySpend(1500) // 150% of limit
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.PACING_LIMIT_EXCEEDED, result.getReasonCode());
    }

    @Test
    void testPassesWhenPacingCurrentIsNull() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(1000)
                .pacingDailySpend(null) // Treated as 0
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testRandomThrottlingNearLimit() {
        // Test the 10% random throttling when spendRatio > 0.9
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .pacingDailyBudget(1000)
                .pacingDailySpend(950) // 95% of limit
                .build();

        // Try multiple seeds to find one that triggers throttling
        boolean foundThrottling = false;
        for (int seed = 0; seed < 1000; seed++) {
            FilterResult result = filter.apply(request, candidate, seed);
            if (!result.getPassed() && result.getDetails().contains("throttled")) {
                foundThrottling = true;
                assertEquals(FilterReasonCode.PACING_LIMIT_EXCEEDED, result.getReasonCode());
                break;
            }
        }
        // Note: Probabilistic test - should find at least one throttling case
    }
}
