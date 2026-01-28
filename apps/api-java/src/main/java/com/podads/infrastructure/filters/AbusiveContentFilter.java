package com.podads.infrastructure.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Abusive Content Filter - Blocks ads based on blocklist.
 * 
 * This filter checks campaign and creative IDs against a blocklist of abusive ads.
 * At Amazon, this would be loaded from S3 and cached in memory, refreshed on events.
 * 
 * For MVP: Simple in-memory HashSet (O(1) lookup).
 * Future: Event-driven refresh from S3 via BlocklistService.
 */
@Component
public class AbusiveContentFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AbusiveContentFilter.class);
    private static final String BLOCKLIST_PATH = "fixtures/blocklist.json";
    
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // In-memory blocklist (O(1) lookup)
    // TODO: Replace with BlocklistService for event-driven refresh
    private final Set<String> blockedCampaignIds = new HashSet<>();
    private final Set<String> blockedCreativeIds = new HashSet<>();
    
    public AbusiveContentFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @PostConstruct
    public void initialize() {
        loadBlocklist();
        logger.info("AbusiveContentFilter initialized", 
            java.util.Map.of("blockedCampaigns", blockedCampaignIds.size(),
                           "blockedCreatives", blockedCreativeIds.size()));
    }
    
    /**
     * Load blocklist from JSON fixture file.
     * Simulates S3 fetch - in production this would be event-driven.
     */
    private void loadBlocklist() {
        try {
            ClassPathResource resource = new ClassPathResource(BLOCKLIST_PATH);
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode sourcesNode = root.get("sources");
            
            // Process customer_reports
            if (sourcesNode.has("customer_reports")) {
                processSource(sourcesNode.get("customer_reports"), "customer_report");
            }
            
            // Process ml_keyword_match
            if (sourcesNode.has("ml_keyword_match")) {
                processSource(sourcesNode.get("ml_keyword_match"), "ml_keyword_match");
            }
            
            // Process manual_curation
            if (sourcesNode.has("manual_curation")) {
                processSource(sourcesNode.get("manual_curation"), "manual_curation");
            }
            
            logger.info("Blocklist loaded successfully", 
                java.util.Map.of("campaigns", blockedCampaignIds.size(),
                               "creatives", blockedCreativeIds.size(),
                               "version", root.get("version").asText()));
        } catch (Exception e) {
            logger.error("Failed to load blocklist from " + BLOCKLIST_PATH + ", using empty blocklist", e);
            // Fail gracefully - continue with empty blocklist
        }
    }
    
    /**
     * Process entries from a blocklist source (customer_reports, ml_keyword_match, manual_curation).
     */
    private void processSource(JsonNode sourceNode, String sourceType) {
        JsonNode entriesNode = sourceNode.get("entries");
        if (entriesNode == null || !entriesNode.isArray()) {
            return;
        }
        
        for (JsonNode entry : entriesNode) {
            if (entry.has("campaignId")) {
                String campaignId = entry.get("campaignId").asText();
                blockedCampaignIds.add(campaignId);
                logger.debug("Added campaign to blocklist", 
                    java.util.Map.of("campaignId", campaignId, "source", sourceType));
            }
            
            if (entry.has("creativeId")) {
                String creativeId = entry.get("creativeId").asText();
                blockedCreativeIds.add(creativeId);
                logger.debug("Added creative to blocklist", 
                    java.util.Map.of("creativeId", creativeId, "source", sourceType));
            }
        }
    }
    
    @Override
    public String getName() {
        return "AbusiveContentFilter";
    }
    
    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        String campaignId = candidate.getCampaign().getId();
        String creativeId = candidate.getCreative().getId();
        
        // Debug: Log when checking abusive ads (only for abusive campaign IDs to reduce noise)
        if (campaignId.startsWith("camp-abuse-")) {
            logger.debug("Checking abusive ad against blocklist", 
                java.util.Map.of("campaignId", campaignId,
                               "creativeId", creativeId,
                               "blocklistSize", blockedCampaignIds.size(),
                               "isBlocked", blockedCampaignIds.contains(campaignId)));
        }
        
        // Check campaign first (faster - blocks entire campaign)
        if (blockedCampaignIds.contains(campaignId)) {
            logger.info("Blocking abusive campaign", 
                java.util.Map.of("campaignId", campaignId, "filter", getName()));
            recordBlocklistHit("campaign", campaignId);
            return FilterResult.builder()
                .passed(false)
                .reasonCode(FilterReasonCode.BRAND_SAFETY_VIOLATION)
                .details("Campaign blocked by brand safety filter: " + campaignId)
                .build();
        }
        
        // Check creative (more granular)
        if (blockedCreativeIds.contains(creativeId)) {
            logger.info("Blocking abusive creative", 
                java.util.Map.of("creativeId", creativeId, "filter", getName()));
            recordBlocklistHit("creative", creativeId);
            return FilterResult.builder()
                .passed(false)
                .reasonCode(FilterReasonCode.BRAND_SAFETY_VIOLATION)
                .details("Creative blocked by brand safety filter: " + creativeId)
                .build();
        }
        
        return FilterResult.builder().passed(true).build();
    }
    
    /**
     * Record blocklist hit metric.
     */
    private void recordBlocklistHit(String blockType, String id) {
        Counter.builder("ad_blocklist_hits_total")
            .description("Total ads blocked by abusive content filter")
            .tag("block_type", blockType)
            .tag("id", id)
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Add campaign to blocklist (for testing/initialization).
     * TODO: Remove when BlocklistService is implemented.
     */
    public void addBlockedCampaign(String campaignId) {
        blockedCampaignIds.add(campaignId);
        logger.debug("Added campaign to blocklist", java.util.Map.of("campaignId", campaignId));
    }
    
    /**
     * Add creative to blocklist (for testing/initialization).
     * TODO: Remove when BlocklistService is implemented.
     */
    public void addBlockedCreative(String creativeId) {
        blockedCreativeIds.add(creativeId);
        logger.debug("Added creative to blocklist", java.util.Map.of("creativeId", creativeId));
    }
}
