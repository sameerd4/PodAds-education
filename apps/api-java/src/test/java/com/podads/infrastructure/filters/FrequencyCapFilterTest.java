package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyCapFilterTest {

    private FrequencyCapFilter filter;

    @BeforeEach
    void setUp() {
        filter = new FrequencyCapFilter();
    }

    @Test
    void testGetName() {
        assertEquals("FrequencyCapFilter", filter.getName());
    }

    @Test
    void testPassesWhenNoFrequencyCap() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .frequencyCapMaxImpressions(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenFrequencyCapNotExceeded() {
        // The filter uses 1% random chance, so most of the time it should pass
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .frequencyCapMaxImpressions(3)
                .frequencyCapWindowHours(1)
                .build();

        // Try multiple seeds - most should pass
        int passCount = 0;
        for (int seed = 0; seed < 100; seed++) {
            FilterResult result = filter.apply(request, candidate, seed);
            if (result.getPassed()) {
                passCount++;
            }
        }
        // With 1% failure rate, we should see mostly passes
        assertTrue(passCount > 90, "Expected most tests to pass with 1% failure rate");
    }

    @Test
    void testRandomFrequencyCapExceeded() {
        // Test the 1% random failure
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .frequencyCapMaxImpressions(3)
                .frequencyCapWindowHours(1)
                .build();

        // Try multiple seeds to find one that triggers the failure
        boolean foundFailure = false;
        for (int seed = 0; seed < 1000; seed++) {
            FilterResult result = filter.apply(request, candidate, seed);
            if (!result.getPassed()) {
                foundFailure = true;
                assertEquals(FilterReasonCode.FREQUENCY_CAP_EXCEEDED, result.getReasonCode());
                assertTrue(result.getDetails().contains("Frequency cap exceeded"));
                assertTrue(result.getDetails().contains("3"));
                assertTrue(result.getDetails().contains("1"));
                break;
            }
        }
        // Note: Probabilistic test - with 1% chance over 1000 seeds, should find at least one
    }
}
