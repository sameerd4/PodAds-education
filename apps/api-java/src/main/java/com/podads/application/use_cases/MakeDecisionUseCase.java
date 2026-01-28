package com.podads.application.use_cases;

import com.podads.application.utils.BrandNameExtractor;
import com.podads.domain.entities.*;
import com.podads.domain.services.AuctionService;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterResult;
import com.podads.infrastructure.filters.*;
import com.podads.infrastructure.sourcing.FixtureSourcingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class MakeDecisionUseCase {
    private static final Logger logger = LoggerFactory.getLogger(MakeDecisionUseCase.class);
    private final FixtureSourcingService sourcingService;
    private final AuctionService auctionService;
    private final List<Filter> allFilters;
    private final MeterRegistry meterRegistry;

    public MakeDecisionUseCase(
            FixtureSourcingService sourcingService,
            AuctionService auctionService,
            MeterRegistry meterRegistry,
            CampaignStatusFilter campaignStatusFilter,
            AbusiveContentFilter abusiveContentFilter,
            ScheduleWindowFilter scheduleWindowFilter,
            SlotTypeFilter slotTypeFilter,
            CreativeApprovalFilter creativeApprovalFilter,
            GeoTargetingFilter geoTargetingFilter,
            DeviceTargetingFilter deviceTargetingFilter,
            TierTargetingFilter tierTargetingFilter,
            CategoryMatchFilter categoryMatchFilter,
            ExcludedCategoryFilter excludedCategoryFilter,
            BudgetRemainingFilter budgetRemainingFilter,
            PacingGateFilter pacingGateFilter,
            FrequencyCapFilter frequencyCapFilter
    ) {
        this.sourcingService = sourcingService;
        this.auctionService = auctionService;
        this.meterRegistry = meterRegistry;
        this.allFilters = List.of(
                campaignStatusFilter,
                abusiveContentFilter, // Early in chain - blocks abusive ads before expensive filters
                scheduleWindowFilter,
                slotTypeFilter,
                creativeApprovalFilter,
                geoTargetingFilter,
                deviceTargetingFilter,
                tierTargetingFilter,
                categoryMatchFilter,
                excludedCategoryFilter,
                budgetRemainingFilter,
                pacingGateFilter,
                frequencyCapFilter
        );
    }

    public Map<String, Object> execute(AdRequest request, int seed) {
        // Start timer for total decision latency
        Timer.Sample decisionTimer = Timer.start(meterRegistry);
        
        String decisionId = "dec-" + System.currentTimeMillis() + "-" + seed;
        MDC.put("decisionId", decisionId);
        
        logger.info("Ad decision started", 
                Map.of("decisionId", decisionId,
                       "requestId", request.getRequestId(),
                       "category", request.getPodcast().getCategory().getValue(),
                       "slotType", request.getSlot().getType().getValue()));
        
        List<Map<String, Object>> stages = new ArrayList<>();
        
        // Extract category and tier for metrics
        String requestCategory = request.getPodcast().getCategory().getValue();
        String requestTier = request.getListener().getTier().getValue();
        
        // Increment total requests counter with category and tier tags
        Counter.builder("ad_requests_total")
                .description("Total ad requests")
                .tag("category", requestCategory)
                .tag("tier", requestTier)
                .register(meterRegistry)
                .increment();

        // Stage 1: Request
        Timer.Sample requestTimer = Timer.start(meterRegistry);
        long requestStageStart = System.nanoTime();
        // Request parsing/validation happens here
        double requestLatency = (System.nanoTime() - requestStageStart) / 1_000_000.0;
        requestTimer.stop(Timer.builder("ad_stage_latency_ms")
                .description("Stage latency in milliseconds")
                .tag("stage", "Request")
                .register(meterRegistry));
        MDC.put("stage", "Request");
        MDC.put("latencyMs", String.format("%.2f", requestLatency));
        logger.debug("Stage completed", Map.of("stage", "Request", "latencyMs", String.format("%.2f", requestLatency)));
        stages.add(createStage("Request",
                requestLatency,
                request.getPodcast().getCategory().getValue() + " / " + request.getSlot().getType().getValue(),
                "Request received for " + request.getPodcast().getShow(),
                Map.of("requestId", request.getRequestId())));

        // Stage 2: Sourcing
        Timer.Sample sourcingTimer = Timer.start(meterRegistry);
        long sourcingStageStart = System.nanoTime();
        List<CandidateAd> candidates = sourcingService.loadCandidates();
        candidates = sourcingService.filterCandidatesByCategory(candidates, request.getPodcast().getCategory());
        double sourcingLatency = (System.nanoTime() - sourcingStageStart) / 1_000_000.0;
        sourcingTimer.stop(Timer.builder("ad_stage_latency_ms")
                .description("Stage latency in milliseconds")
                .tag("stage", "Sourcing")
                .register(meterRegistry));
        
        // Record candidates processed
        DistributionSummary.builder("ad_candidates_processed")
                .description("Number of candidates processed per request")
                .register(meterRegistry)
                .record(candidates.size());
        
        MDC.put("stage", "Sourcing");
        MDC.put("latencyMs", String.format("%.2f", sourcingLatency));
        logger.debug("Stage completed", Map.of("stage", "Sourcing", 
                                               "latencyMs", String.format("%.2f", sourcingLatency),
                                               "candidatesFound", String.valueOf(candidates.size())));
        stages.add(createStage("Sourcing",
                sourcingLatency,
                "Category: " + request.getPodcast().getCategory().getValue(),
                "Found " + candidates.size() + " candidate ads",
                Map.of("candidateCount", candidates.size())));

        // Stage 3: Filters
        Timer.Sample filterTimer = Timer.start(meterRegistry);
        long filterStageStart = System.nanoTime();
        Map<String, Map<String, Map<String, Object>>> filterResults = new HashMap<>();
        List<CandidateAd> passedCandidates = new ArrayList<>();

        for (CandidateAd candidate : candidates) {
            String candidateId = candidate.getCampaign().getId() + "-" + candidate.getCreative().getId();
            Map<String, Map<String, Object>> results = new HashMap<>();

            boolean allPassed = true;
            for (Filter filter : allFilters) {
                FilterResult result = filter.apply(request, candidate, seed);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("passed", result.getPassed());
                if (result.getReasonCode() != null) {
                    resultMap.put("reasonCode", result.getReasonCode().getValue());
                }
                if (result.getDetails() != null) {
                    resultMap.put("details", result.getDetails());
                }
                results.put(filter.getName(), resultMap);

                // Record filter application metric
                Counter.builder("ad_filters_applied")
                        .description("Filter applications")
                        .tag("filter_name", filter.getName())
                        .tag("passed", String.valueOf(result.getPassed()))
                        .register(meterRegistry)
                        .increment();

                if (!result.getPassed()) {
                    allPassed = false;
                    break; // Short-circuit on first failure
                }
            }

            filterResults.put(candidateId, results);
            if (allPassed) {
                passedCandidates.add(candidate);
            }
        }

        double filterLatency = (System.nanoTime() - filterStageStart) / 1_000_000.0;
        filterTimer.stop(Timer.builder("ad_stage_latency_ms")
                .description("Stage latency in milliseconds")
                .tag("stage", "Filters")
                .register(meterRegistry));
        int dropCount = candidates.size() - passedCandidates.size();
        MDC.put("stage", "Filters");
        MDC.put("latencyMs", String.format("%.2f", filterLatency));
        logger.debug("Stage completed", Map.of("stage", "Filters",
                                              "latencyMs", String.format("%.2f", filterLatency),
                                              "passedCount", String.valueOf(passedCandidates.size()),
                                              "droppedCount", String.valueOf(dropCount)));

        // Aggregate filter failures
        Map<String, Integer> filterFailures = new HashMap<>();
        for (Map<String, Map<String, Object>> results : filterResults.values()) {
            for (Map.Entry<String, Map<String, Object>> entry : results.entrySet()) {
                if (!(Boolean) entry.getValue().get("passed")) {
                    filterFailures.put(entry.getKey(), filterFailures.getOrDefault(entry.getKey(), 0) + 1);
                    break;
                }
            }
        }

        stages.add(createStage("Filters",
                filterLatency,
                candidates.size() + " candidates",
                passedCandidates.size() + " passed, " + dropCount + " dropped",
                Map.of(
                        "totalCandidates", candidates.size(),
                        "passedCount", passedCandidates.size(),
                        "droppedCount", dropCount,
                        "filterFailures", filterFailures
                )));

        // Stage 4: Auction
        Timer.Sample auctionTimer = Timer.start(meterRegistry);
        long auctionStageStart = System.nanoTime();
        List<Map<String, Object>> scoredCandidates = new ArrayList<>();

        for (CandidateAd candidate : passedCandidates) {
            String candidateId = candidate.getCampaign().getId() + "-" + candidate.getCreative().getId();
            Map<String, Object> score = auctionService.scoreCandidate(request, candidate);

            // Extract and normalize brand name from campaign name (e.g., "Capital One Venture..." -> "Capital One")
            String campaignName = candidate.getCampaign().getName();
            String brandName = BrandNameExtractor.extractAndNormalizeBrandName(campaignName);
            if (brandName == null || brandName.isEmpty()) {
                brandName = candidate.getCampaign().getId(); // Fallback to campaign ID if extraction fails
            }

            Map<String, Object> scoredCandidate = new HashMap<>();
            scoredCandidate.put("candidateId", candidateId);
            scoredCandidate.put("campaignId", candidate.getCampaign().getId());
            scoredCandidate.put("campaignName", campaignName);
            scoredCandidate.put("brandName", brandName);
            scoredCandidate.put("creativeId", candidate.getCreative().getId());
            scoredCandidate.put("filterResults", filterResults.get(candidateId));
            scoredCandidate.put("score", score);
            scoredCandidate.put("passedAllFilters", true);
            scoredCandidates.add(scoredCandidate);
        }

        // Include failed candidates with zero scores for explainability
        // This allows frontend to show "why did this ad get filtered?" even though it didn't reach auction
        for (CandidateAd candidate : candidates) {
            String candidateId = candidate.getCampaign().getId() + "-" + candidate.getCreative().getId();
            Map<String, Map<String, Object>> filters = filterResults.get(candidateId);
            boolean passedAll = filters != null && filters.values().stream()
                    .allMatch(r -> (Boolean) r.get("passed"));

            if (!passedAll && scoredCandidates.stream().noneMatch(c -> c.get("candidateId").equals(candidateId))) {
                // Extract and normalize brand name from campaign name
                String campaignName = candidate.getCampaign().getName();
                String brandName = BrandNameExtractor.extractAndNormalizeBrandName(campaignName);
                if (brandName == null || brandName.isEmpty()) {
                    brandName = candidate.getCampaign().getId(); // Fallback to campaign ID if extraction fails
                }

                Map<String, Object> failedCandidate = new HashMap<>();
                failedCandidate.put("candidateId", candidateId);
                failedCandidate.put("campaignId", candidate.getCampaign().getId());
                failedCandidate.put("campaignName", campaignName);
                failedCandidate.put("brandName", brandName);
                failedCandidate.put("creativeId", candidate.getCreative().getId());
                failedCandidate.put("filterResults", filters != null ? filters : Map.of());
                
                Map<String, Object> zeroScore = new HashMap<>();
                zeroScore.put("bidCpm", (double) candidate.getCampaign().getBidCpm());
                zeroScore.put("matchScore", 0.0);
                zeroScore.put("pacingMultiplier", 0.0);
                zeroScore.put("finalScore", 0.0);
                Map<String, Object> breakdown = new HashMap<>();
                breakdown.put("categoryMatch", 0.0);
                breakdown.put("showMatch", 0.0);
                breakdown.put("listenerSegmentWeight", 1.0);
                zeroScore.put("breakdown", breakdown);
                failedCandidate.put("score", zeroScore);
                failedCandidate.put("passedAllFilters", false);
                scoredCandidates.add(failedCandidate);
            }
        }

        // Sort by final score (descending) - highest score wins
        scoredCandidates.sort((a, b) -> {
            double scoreA = (Double) ((Map<String, Object>) a.get("score")).get("finalScore");
            double scoreB = (Double) ((Map<String, Object>) b.get("score")).get("finalScore");
            return Double.compare(scoreB, scoreA);
        });

        double auctionLatency = (System.nanoTime() - auctionStageStart) / 1_000_000.0;
        auctionTimer.stop(Timer.builder("ad_stage_latency_ms")
                .description("Stage latency in milliseconds")
                .tag("stage", "Auction")
                .register(meterRegistry));
        MDC.put("stage", "Auction");
        MDC.put("latencyMs", String.format("%.2f", auctionLatency));
        logger.debug("Stage completed", Map.of("stage", "Auction",
                                              "latencyMs", String.format("%.2f", auctionLatency),
                                              "candidatesScored", String.valueOf(scoredCandidates.size())));
        // Winner selection: must have candidates AND top score > 0 (no zero/negative scores)
        // Zero scores occur when pacingMultiplier = 0 (budget exhausted) or matchScore = 0
        Map<String, Object> winner = scoredCandidates.isEmpty() || 
                (Double) ((Map<String, Object>) scoredCandidates.get(0).get("score")).get("finalScore") <= 0
                ? null : scoredCandidates.get(0);

        stages.add(createStage("Auction",
                auctionLatency,
                passedCandidates.size() + " eligible candidates",
                winner != null
                        ? "Winner: " + (winner.get("brandName") != null ? winner.get("brandName") : winner.get("campaignId")) 
                        + " (" + winner.get("campaignId") + ") - score: " +
                        String.format("%.2f", ((Map<String, Object>) winner.get("score")).get("finalScore"))
                        : "No winner",
                Map.of(
                        "scoredCount", (long) scoredCandidates.stream()
                                .filter(c -> (Double) ((Map<String, Object>) c.get("score")).get("finalScore") > 0)
                                .count(),
                        "topScore", winner != null ? ((Map<String, Object>) winner.get("score")).get("finalScore") : null
                )));

        // Stage 5: Serve
        Timer.Sample serveTimer = Timer.start(meterRegistry);
        long serveStageStart = System.nanoTime();
        Map<String, Object> serveInstruction = null;
        if (winner != null) {
            CandidateAd winningCandidate = passedCandidates.stream()
                    .filter(c -> c.getCampaign().getId().equals(winner.get("campaignId")) &&
                            c.getCreative().getId().equals(winner.get("creativeId")))
                    .findFirst()
                    .orElseThrow();

            String baseUrl = "https://tracking.podads.lab/events/" + decisionId;
            serveInstruction = new HashMap<>();
            serveInstruction.put("creativeId", winner.get("creativeId"));
            serveInstruction.put("campaignId", winner.get("campaignId"));
            serveInstruction.put("campaignName", winner.get("campaignName"));
            serveInstruction.put("brandName", winner.get("brandName"));
            serveInstruction.put("assetUrl", winningCandidate.getCreative().getAssetUrl());
            serveInstruction.put("durationSeconds", winningCandidate.getCreative().getDurationSeconds());
            Map<String, Object> trackingUrls = new HashMap<>();
            trackingUrls.put("impression", baseUrl + "/impression");
            trackingUrls.put("quartiles", List.of(
                    baseUrl + "/quartile/25",
                    baseUrl + "/quartile/50",
                    baseUrl + "/quartile/75",
                    baseUrl + "/quartile/100"
            ));
            trackingUrls.put("complete", baseUrl + "/complete");
            trackingUrls.put("click", baseUrl + "/click");
            serveInstruction.put("trackingUrls", trackingUrls);

            double pricePaid = scoredCandidates.size() > 1 &&
                    (Double) ((Map<String, Object>) scoredCandidates.get(1).get("score")).get("finalScore") > 0
                    ? (Double) ((Map<String, Object>) scoredCandidates.get(1).get("score")).get("bidCpm")
                    : (Double) ((Map<String, Object>) winner.get("score")).get("bidCpm");
            serveInstruction.put("pricePaid", pricePaid);
        }

        double serveLatency = (System.nanoTime() - serveStageStart) / 1_000_000.0;
        serveTimer.stop(Timer.builder("ad_stage_latency_ms")
                .description("Stage latency in milliseconds")
                .tag("stage", "Serve")
                .register(meterRegistry));
        MDC.put("stage", "Serve");
        MDC.put("latencyMs", String.format("%.2f", serveLatency));
        logger.debug("Stage completed", Map.of("stage", "Serve",
                                              "latencyMs", String.format("%.2f", serveLatency),
                                              "served", winner != null ? "true" : "false"));
        stages.add(createStage("Serve",
                serveLatency,
                winner != null ? "Winner: " + (winner.get("brandName") != null ? winner.get("brandName") : winner.get("campaignId")) 
                        + " (" + winner.get("campaignId") + ")" : "No winner",
                serveInstruction != null
                        ? "Serving " + (serveInstruction.get("brandName") != null ? serveInstruction.get("brandName") : serveInstruction.get("campaignId")) 
                        + " creative " + serveInstruction.get("creativeId")
                        : "No fill",
                Map.of(
                        "served", winner != null,
                        "pricePaid", serveInstruction != null ? serveInstruction.get("pricePaid") : null
                )));

        // Record total decision latency
        double totalLatency = decisionTimer.stop(Timer.builder("ad_decision_latency_ms")
                .description("Total ad decision latency in milliseconds")
                .register(meterRegistry)) / 1_000_000.0;
        
        // Record decision outcome
        String outcome = (winner != null && serveInstruction != null) ? "fill" : "no_fill";
        String category = request.getPodcast().getCategory().getValue();
        String slotType = request.getSlot().getType().getValue();
        
        Counter.builder("ad_decisions_total")
                .description("Total ad decisions")
                .tag("outcome", outcome)
                .tag("category", category)
                .tag("slot_type", slotType)
                .register(meterRegistry)
                .increment();
        
        // Business metrics: Campaign performance (if winner exists)
        if (winner != null && serveInstruction != null) {
            String campaignId = (String) winner.get("campaignId");
            if (campaignId != null) {
                Counter.builder("ad_campaign_served_total")
                        .description("Total ads served per campaign")
                        .tag("campaign_id", campaignId)
                        .tag("category", category)
                        .tag("slot_type", slotType)
                        .register(meterRegistry)
                        .increment();
                
                // Revenue metric: pricePaid is in cents, revenue per impression = pricePaid / 100000
                Object pricePaidObj = serveInstruction.get("pricePaid");
                if (pricePaidObj != null) {
                    double pricePaidCents = pricePaidObj instanceof Number 
                        ? ((Number) pricePaidObj).doubleValue() 
                        : Double.parseDouble(pricePaidObj.toString());
                    double revenueDollars = pricePaidCents / 100000.0;
                    
                    Counter.builder("ad_revenue_total")
                            .description("Total revenue in dollars")
                            .tag("category", category)
                            .tag("slot_type", slotType)
                            .tag("campaign_id", campaignId)
                            .register(meterRegistry)
                            .increment(revenueDollars);
                }
            }
        }
        
        // Business metrics: Category efficiency (fill rate by category)
        if (outcome.equals("fill")) {
            Counter.builder("ad_category_fills_total")
                    .description("Total fills by category")
                    .tag("category", category)
                    .tag("slot_type", slotType)
                    .register(meterRegistry)
                    .increment();
        }
        
        // Business metrics: Slot-type performance
        Counter.builder("ad_slot_decisions_total")
                .description("Total decisions by slot type")
                .tag("slot_type", slotType)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
        
        MDC.put("fillRate", outcome.equals("fill") ? "1.0" : "0.0");
        logger.info("Ad decision completed", 
                Map.of("decisionId", decisionId,
                       "outcome", outcome,
                       "totalLatencyMs", String.format("%.2f", totalLatency),
                       "fillRate", outcome.equals("fill") ? "1.0" : "0.0"));

        Map<String, Object> decision = new HashMap<>();
        decision.put("decisionId", decisionId);
        decision.put("requestId", request.getRequestId());
        decision.put("seed", seed);
        decision.put("timestamp", Instant.now().toString());
        decision.put("stages", stages);
        decision.put("candidates", scoredCandidates);
        if (winner != null && serveInstruction != null) {
            Map<String, Object> winnerMap = new HashMap<>();
            winnerMap.put("candidate", winner);
            winnerMap.put("serve", serveInstruction);
            decision.put("winner", winnerMap);
        } else {
            decision.put("winner", null);
            decision.put("noFillReason", "No eligible candidates after filtering");
        }

        return decision;
    }

    private Map<String, Object> createStage(String name, double latencyMs, String inputSummary,
                                             String outputSummary, Map<String, Object> debugPayload) {
        Map<String, Object> stage = new HashMap<>();
        stage.put("stageName", name);
        stage.put("latencyMs", latencyMs);
        stage.put("inputSummary", inputSummary);
        stage.put("outputSummary", outputSummary);
        stage.put("debugPayload", debugPayload);
        return stage;
    }
}


