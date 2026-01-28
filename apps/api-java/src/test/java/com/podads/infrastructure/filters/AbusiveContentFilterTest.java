package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbusiveContentFilterTest {
    private AbusiveContentFilter filter;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        filter = new AbusiveContentFilter(meterRegistry);
    }

    @Test
    void testPassesWhenNotBlocked() {
        // Given: A candidate ad that is not in the blocklist
        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd().build();

        // When: Filter is applied
        FilterResult result = filter.apply(request, candidate, 12345);

        // Then: Ad passes
        assertTrue(result.getPassed());
        assertNull(result.getReasonCode());
    }

    @Test
    void testBlocksCampaignWhenInBlocklist() {
        // Given: A campaign that is blocked
        String blockedCampaignId = "camp-blocked-001";
        filter.addBlockedCampaign(blockedCampaignId);

        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .campaignId(blockedCampaignId)
                .build();

        // When: Filter is applied
        FilterResult result = filter.apply(request, candidate, 12345);

        // Then: Ad is blocked
        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.BRAND_SAFETY_VIOLATION, result.getReasonCode());
        assertTrue(result.getDetails().contains("Campaign blocked"));
        assertTrue(result.getDetails().contains(blockedCampaignId));
    }

    @Test
    void testBlocksCreativeWhenInBlocklist() {
        // Given: A creative that is blocked (but campaign is not)
        String blockedCreativeId = "creat-blocked-001";
        filter.addBlockedCreative(blockedCreativeId);

        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .creativeId(blockedCreativeId)
                .build();

        // When: Filter is applied
        FilterResult result = filter.apply(request, candidate, 12345);

        // Then: Ad is blocked
        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.BRAND_SAFETY_VIOLATION, result.getReasonCode());
        assertTrue(result.getDetails().contains("Creative blocked"));
        assertTrue(result.getDetails().contains(blockedCreativeId));
    }

    @Test
    void testCampaignBlockTakesPrecedence() {
        // Given: Both campaign and creative are blocked
        String blockedCampaignId = "camp-blocked-002";
        String blockedCreativeId = "creat-blocked-002";
        filter.addBlockedCampaign(blockedCampaignId);
        filter.addBlockedCreative(blockedCreativeId);

        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .campaignId(blockedCampaignId)
                .creativeId(blockedCreativeId)
                .build();

        // When: Filter is applied
        FilterResult result = filter.apply(request, candidate, 12345);

        // Then: Campaign block takes precedence (checked first)
        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.BRAND_SAFETY_VIOLATION, result.getReasonCode());
        assertTrue(result.getDetails().contains("Campaign blocked"));
        assertTrue(result.getDetails().contains(blockedCampaignId));
    }

    @Test
    void testRecordsMetricsOnBlock() {
        // Given: A blocked campaign
        String blockedCampaignId = "camp-metrics-test";
        filter.addBlockedCampaign(blockedCampaignId);

        AdRequest request = TestDataBuilder.adRequest().build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .campaignId(blockedCampaignId)
                .build();

        // When: Filter is applied
        filter.apply(request, candidate, 12345);

        // Then: Metric is recorded
        double count = meterRegistry.counter("ad_blocklist_hits_total",
            "block_type", "campaign",
            "id", blockedCampaignId).count();
        assertEquals(1.0, count);
    }
}
