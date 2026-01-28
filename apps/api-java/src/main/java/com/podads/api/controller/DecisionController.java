package com.podads.api.controller;

import com.podads.api.dto.AdRequestDto;
import com.podads.application.use_cases.MakeDecisionUseCase;
import com.podads.domain.entities.AdRequest;
import com.podads.domain.valueobjects.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
// CORS is handled globally by CorsConfig - removing redundant annotation
public class DecisionController {
    private static final Logger logger = LoggerFactory.getLogger(DecisionController.class);
    private final MakeDecisionUseCase makeDecisionUseCase;
    private final MeterRegistry meterRegistry;

    public DecisionController(MakeDecisionUseCase makeDecisionUseCase, MeterRegistry meterRegistry) {
        this.makeDecisionUseCase = makeDecisionUseCase;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/decision")
    public ResponseEntity<?> makeDecision(
            @Valid @RequestBody AdRequestDto requestDto,
            @RequestParam(defaultValue = "12345") int seed
    ) {
        // Set correlation ID in MDC for structured logging
        String requestId = requestDto.getRequestId();
        MDC.put("requestId", requestId);
        
        logger.info("Ad request received", 
                Map.of("requestId", requestId, 
                       "category", requestDto.getPodcast().getCategory().getValue(),
                       "slotType", requestDto.getSlot().getType().getValue()));
        
        Timer.Sample requestTimer = Timer.start(meterRegistry);
        try {
            // Convert DTO to domain entity
            AdRequest request = new AdRequest(
                    requestDto.getRequestId(),
                    new PodcastContext(
                            requestDto.getPodcast().getCategory(),
                            requestDto.getPodcast().getShow(),
                            requestDto.getPodcast().getEpisode()
                    ),
                    new SlotContext(
                            requestDto.getSlot().getType(),
                            requestDto.getSlot().getCuePoint()
                    ),
                    new ListenerContext(
                            requestDto.getListener().getGeo(),
                            requestDto.getListener().getDevice(),
                            requestDto.getListener().getTier(),
                            requestDto.getListener().getConsent(),
                            requestDto.getListener().getTimeOfDay()
                    ),
                    Instant.parse(requestDto.getTimestamp().replace("Z", "+00:00"))
            );

            var decision = makeDecisionUseCase.execute(request, seed);
            String decisionId = (String) decision.get("decisionId");
            MDC.put("decisionId", decisionId);
            
            double totalLatency = requestTimer.stop(Timer.builder("http_server_requests")
                    .description("HTTP request duration")
                    .tag("method", "POST")
                    .tag("uri", "/v1/decision")
                    .tag("status", "200")
                    .tag("outcome", "SUCCESS")
                    .register(meterRegistry)) / 1_000_000.0; // Convert to milliseconds
            
            boolean hasWinner = decision.get("winner") != null;
            MDC.put("fillRate", hasWinner ? "1.0" : "0.0");
            
            logger.info("Ad decision completed", 
                    Map.of("requestId", requestId,
                           "decisionId", decisionId,
                           "latencyMs", String.format("%.2f", totalLatency),
                           "fillRate", hasWinner ? "1.0" : "0.0",
                           "outcome", hasWinner ? "fill" : "no_fill"));
            
            MDC.clear();
            return ResponseEntity.ok(decision);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            String errorType = e.getClass().getSimpleName();
            
            // Record error metrics
            Counter.builder("ad_errors_total")
                    .description("Total errors")
                    .tag("error_type", errorType)
                    .tag("stage", "controller")
                    .register(meterRegistry)
                    .increment();
            
            Counter.builder("ad_http_errors_total")
                    .description("HTTP errors")
                    .tag("status_code", "500")
                    .register(meterRegistry)
                    .increment();
            
            requestTimer.stop(Timer.builder("http_server_requests")
                    .description("HTTP request duration")
                    .tag("method", "POST")
                    .tag("uri", "/v1/decision")
                    .tag("status", "500")
                    .tag("outcome", "SERVER_ERROR")
                    .register(meterRegistry));
            
            // Log error with correlation IDs
            logger.error("Ad decision failed", 
                    Map.of("requestId", requestId,
                           "errorType", errorType,
                           "errorMessage", errorMessage),
                    e);
            
            MDC.clear();
            return ResponseEntity.status(500).body(Map.of("error", errorMessage));
        }
    }

    @PostMapping("/decision/batch")
    public ResponseEntity<?> makeBatchDecision(
            @Valid @RequestBody AdRequestDto requestDto,
            @RequestParam(defaultValue = "12345") int seed,
            @RequestParam(defaultValue = "100") int count
    ) {
        // Limit batch size to prevent abuse
        if (count > 1000) {
            count = 1000;
        }
        if (count < 1) {
            count = 1;
        }

        String requestId = requestDto.getRequestId();
        MDC.put("requestId", requestId);
        
        logger.info("Batch ad request received", 
                Map.of("requestId", requestId, 
                       "count", String.valueOf(count),
                       "category", requestDto.getPodcast().getCategory().getValue(),
                       "slotType", requestDto.getSlot().getType().getValue()));
        
        Timer.Sample batchTimer = Timer.start(meterRegistry);
        List<Map<String, Object>> decisions = new ArrayList<>();
        
        try {
            // Convert DTO to domain entity (base request)
            AdRequest baseRequest = new AdRequest(
                    requestDto.getRequestId(),
                    new PodcastContext(
                            requestDto.getPodcast().getCategory(),
                            requestDto.getPodcast().getShow(),
                            requestDto.getPodcast().getEpisode()
                    ),
                    new SlotContext(
                            requestDto.getSlot().getType(),
                            requestDto.getSlot().getCuePoint()
                    ),
                    new ListenerContext(
                            requestDto.getListener().getGeo(),
                            requestDto.getListener().getDevice(),
                            requestDto.getListener().getTier(),
                            requestDto.getListener().getConsent(),
                            requestDto.getListener().getTimeOfDay()
                    ),
                    Instant.parse(requestDto.getTimestamp().replace("Z", "+00:00"))
            );

            // Process batch: each decision gets a unique seed (seed + index)
            for (int i = 0; i < count; i++) {
                // Create unique request ID for each decision in batch
                AdRequest request = new AdRequest(
                        requestDto.getRequestId() + "-batch-" + i,
                        baseRequest.getPodcast(),
                        baseRequest.getSlot(),
                        baseRequest.getListener(),
                        baseRequest.getTimestamp()
                );
                
                var decision = makeDecisionUseCase.execute(request, seed + i);
                decisions.add(decision);
            }

            double totalLatency = batchTimer.stop(Timer.builder("http_server_requests")
                    .description("HTTP request duration")
                    .tag("method", "POST")
                    .tag("uri", "/v1/decision/batch")
                    .tag("status", "200")
                    .tag("outcome", "SUCCESS")
                    .register(meterRegistry)) / 1_000_000.0;

            int fills = (int) decisions.stream()
                    .filter(d -> d.get("winner") != null)
                    .count();
            
            logger.info("Batch ad decision completed", 
                    Map.of("requestId", requestId,
                           "count", String.valueOf(count),
                           "fills", String.valueOf(fills),
                           "latencyMs", String.format("%.2f", totalLatency),
                           "avgLatencyMs", String.format("%.2f", totalLatency / count)));

            MDC.clear();
            return ResponseEntity.ok(Map.of(
                    "decisions", decisions,
                    "count", count,
                    "fills", fills,
                    "noFills", count - fills,
                    "totalLatencyMs", totalLatency
            ));
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            String errorType = e.getClass().getSimpleName();
            
            batchTimer.stop(Timer.builder("http_server_requests")
                    .description("HTTP request duration")
                    .tag("method", "POST")
                    .tag("uri", "/v1/decision/batch")
                    .tag("status", "500")
                    .tag("outcome", "SERVER_ERROR")
                    .register(meterRegistry));
            
            logger.error("Batch ad decision failed", 
                    Map.of("requestId", requestId,
                           "count", String.valueOf(count),
                           "errorType", errorType,
                           "errorMessage", errorMessage),
                    e);
            
            MDC.clear();
            return ResponseEntity.status(500).body(Map.of("error", errorMessage));
        }
    }
}

