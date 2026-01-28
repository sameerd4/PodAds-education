package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleWindowFilterTest {

    private ScheduleWindowFilter filter;
    private Instant now;

    @BeforeEach
    void setUp() {
        filter = new ScheduleWindowFilter();
        now = Instant.now();
    }

    @Test
    void testGetName() {
        assertEquals("ScheduleWindowFilter", filter.getName());
    }

    @Test
    void testPassesWhenWithinScheduleWindow() {
        AdRequest request = TestDataBuilder.adRequest()
                .timestamp(now)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .startDate(now.minusSeconds(86400)) // 1 day ago
                .endDate(now.plusSeconds(86400 * 30)) // 30 days from now
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
        assertNull(result.getReasonCode());
    }

    @Test
    void testFailsWhenBeforeStartDate() {
        AdRequest request = TestDataBuilder.adRequest()
                .timestamp(now)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .startDate(now.plusSeconds(86400)) // 1 day in future
                .endDate(now.plusSeconds(86400 * 30))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.OUTSIDE_SCHEDULE_WINDOW, result.getReasonCode());
        assertTrue(result.getDetails().contains("starts on"));
    }

    @Test
    void testFailsWhenAfterEndDate() {
        AdRequest request = TestDataBuilder.adRequest()
                .timestamp(now)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .startDate(now.minusSeconds(86400 * 30)) // 30 days ago
                .endDate(now.minusSeconds(86400)) // 1 day ago
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.OUTSIDE_SCHEDULE_WINDOW, result.getReasonCode());
        assertTrue(result.getDetails().contains("ended on"));
    }

    @Test
    void testPassesWhenExactlyAtStartDate() {
        AdRequest request = TestDataBuilder.adRequest()
                .timestamp(now)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .startDate(now)
                .endDate(now.plusSeconds(86400 * 30))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenExactlyAtEndDate() {
        AdRequest request = TestDataBuilder.adRequest()
                .timestamp(now)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .startDate(now.minusSeconds(86400 * 30))
                .endDate(now)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
