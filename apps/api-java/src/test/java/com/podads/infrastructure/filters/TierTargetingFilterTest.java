package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import com.podads.domain.valueobjects.TierType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TierTargetingFilterTest {

    private TierTargetingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TierTargetingFilter();
    }

    @Test
    void testGetName() {
        assertEquals("TierTargetingFilter", filter.getName());
    }

    @Test
    void testPassesWhenTierMatches() {
        AdRequest request = TestDataBuilder.adRequest()
                .tier(TierType.FREE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetTiers(List.of(TierType.FREE))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenTierInMultipleTargets() {
        AdRequest request = TestDataBuilder.adRequest()
                .tier(TierType.PREMIUM)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetTiers(List.of(TierType.FREE, TierType.PREMIUM))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenTierDoesNotMatch() {
        AdRequest request = TestDataBuilder.adRequest()
                .tier(TierType.FREE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetTiers(List.of(TierType.PREMIUM))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.TIER_MISMATCH, result.getReasonCode());
        assertTrue(result.getDetails().contains("FREE"));
        assertTrue(result.getDetails().contains("not in targeting list"));
    }

    @Test
    void testPassesWhenNoTierTargeting() {
        AdRequest request = TestDataBuilder.adRequest()
                .tier(TierType.FREE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetTiers(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenTierTargetingIsNull() {
        AdRequest request = TestDataBuilder.adRequest()
                .tier(TierType.FREE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetTiers(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
