package com.podads.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    
    private final String prometheusUrl;
    private final RestTemplate restTemplate;
    
    public MetricsController() {
        // Read from environment variable, fallback to localhost for dev
        this.prometheusUrl = Objects.requireNonNullElse(
            System.getenv("PROMETHEUS_URL"),
            "http://localhost:9090"
        );
        this.restTemplate = new RestTemplate();
        
        logger.info("MetricsController initialized with Prometheus URL: {}", prometheusUrl);
    }
    
    /**
     * Proxy for Prometheus instant query API
     * GET /api/metrics/query?query=<promql>&time=<optional>
     */
    @GetMapping("/query")
    public ResponseEntity<String> queryPrometheus(
            @RequestParam String query,
            @RequestParam(required = false) String time) {
        
        try {
            // Build Prometheus query URL using UriComponentsBuilder for proper encoding
            UriComponentsBuilder urlBuilder = UriComponentsBuilder
                    .fromHttpUrl(prometheusUrl + "/api/v1/query")
                    .queryParam("query", query);
            
            if (time != null && !time.isEmpty()) {
                urlBuilder.queryParam("time", time);
            }
            
            // Use toUri() instead of toUriString() to avoid double-encoding issues
            java.net.URI uri = urlBuilder.build().toUri();
            logger.debug("Proxying Prometheus query: {} -> {}", query, uri);
            
            // Proxy request to Prometheus using URI object directly
            String response = restTemplate.getForObject(uri, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Failed to query Prometheus", e);
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"errorType\":\"internal\",\"error\":\"Failed to query Prometheus: " + 
                          e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
    
    /**
     * Proxy for Prometheus range query API
     * GET /api/metrics/query_range?query=<promql>&start=<timestamp>&end=<timestamp>&step=<duration>
     */
    @GetMapping("/query_range")
    public ResponseEntity<String> queryRange(
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "15s") String step) {
        
        try {
            // Build Prometheus query_range URL using UriComponentsBuilder for proper encoding
            java.net.URI uri = UriComponentsBuilder
                    .fromHttpUrl(prometheusUrl + "/api/v1/query_range")
                    .queryParam("query", query)
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParam("step", step)
                    .build()
                    .toUri();
            
            logger.debug("Proxying Prometheus range query: {} from {} to {} -> {}", query, start, end, uri);
            
            // Proxy request to Prometheus using URI object directly
            String response = restTemplate.getForObject(uri, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Failed to query Prometheus range", e);
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"errorType\":\"internal\",\"error\":\"Failed to query Prometheus: " + 
                          e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}
