package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.Creative;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreativeApprovalFilterTest {

    private CreativeApprovalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CreativeApprovalFilter();
    }

    @Test
    void testGetName() {
        assertEquals("CreativeApprovalFilter", filter.getName());
    }

    @Test
    void testPassesWhenCreativeIsApproved() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .creativeApprovalStatus(Creative.ApprovalStatus.APPROVED)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
        assertNull(result.getReasonCode());
    }

    @Test
    void testFailsWhenCreativeIsPending() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .creativeApprovalStatus(Creative.ApprovalStatus.PENDING)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CREATIVE_NOT_APPROVED, result.getReasonCode());
        assertTrue(result.getDetails().contains("PENDING"));
    }

    @Test
    void testFailsWhenCreativeIsRejected() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .creativeApprovalStatus(Creative.ApprovalStatus.REJECTED)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CREATIVE_NOT_APPROVED, result.getReasonCode());
        assertTrue(result.getDetails().contains("REJECTED"));
    }
}
