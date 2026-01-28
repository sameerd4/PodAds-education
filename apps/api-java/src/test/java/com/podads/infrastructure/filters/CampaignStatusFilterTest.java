package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.Campaign;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampaignStatusFilterTest {

    private CampaignStatusFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CampaignStatusFilter();
    }

    @Test
    void testGetName() {
        assertEquals("CampaignStatusFilter", filter.getName());
    }

    @Test
    void testPassesWhenCampaignIsActive() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .status(Campaign.CampaignStatus.ACTIVE)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
        assertNull(result.getReasonCode());
    }

    @Test
    void testFailsWhenCampaignIsPaused() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .status(Campaign.CampaignStatus.PAUSED)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CAMPAIGN_INACTIVE, result.getReasonCode());
        assertTrue(result.getDetails().contains("PAUSED"));
    }

    @Test
    void testFailsWhenCampaignIsEnded() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .status(Campaign.CampaignStatus.ENDED)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CAMPAIGN_INACTIVE, result.getReasonCode());
        assertTrue(result.getDetails().contains("ENDED"));
    }

    @Test
    void testFailsWhenCampaignIsDraft() {
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CAMPAIGN_INACTIVE, result.getReasonCode());
        assertTrue(result.getDetails().contains("DRAFT"));
    }
}
