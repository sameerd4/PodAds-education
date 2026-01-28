package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoTargetingFilterTest {

    private GeoTargetingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GeoTargetingFilter();
    }

    @Test
    void testGetName() {
        assertEquals("GeoTargetingFilter", filter.getName());
    }

    @Test
    void testPassesWhenGeoMatches() {
        AdRequest request = TestDataBuilder.adRequest()
                .geo("US")
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetGeo(List.of("US"))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenGeoInMultipleTargets() {
        AdRequest request = TestDataBuilder.adRequest()
                .geo("CA")
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetGeo(List.of("US", "CA", "MX"))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenGeoDoesNotMatch() {
        AdRequest request = TestDataBuilder.adRequest()
                .geo("US")
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetGeo(List.of("CA"))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.GEO_MISMATCH, result.getReasonCode());
        assertTrue(result.getDetails().contains("US"));
        assertTrue(result.getDetails().contains("not in targeting list"));
    }

    @Test
    void testPassesWhenNoGeoTargeting() {
        AdRequest request = TestDataBuilder.adRequest()
                .geo("US")
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetGeo(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenGeoTargetingIsNull() {
        AdRequest request = TestDataBuilder.adRequest()
                .geo("US")
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetGeo(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
