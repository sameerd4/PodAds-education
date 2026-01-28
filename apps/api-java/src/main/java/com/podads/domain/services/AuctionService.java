package com.podads.domain.services;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuctionService {

    public Map<String, Object> calculateMatchScore(AdRequest request, CandidateAd candidate) {
        // Default scores: 0.5 = neutral (no targeting = no penalty/boost)
        double categoryMatch = 0.5;
        double showMatch = 0.5;
        double listenerSegmentWeight = 1.0;

        // Category match: 1.0 = exact match, 0.3 = partial (campaign targets but not this category)
        var categories = candidate.getCampaign().getTargeting().getCategories();
        if (categories != null && !categories.isEmpty()) {
            if (categories.contains(request.getPodcast().getCategory())) {
                categoryMatch = 1.0;
            } else {
                categoryMatch = 0.3; // Partial match: campaign has targeting but not this category
            }
        }

        // Show match: 1.0 = exact match, 0.4 = partial (campaign targets shows but not this one)
        var shows = candidate.getCampaign().getTargeting().getShows();
        if (shows != null && !shows.isEmpty()) {
            if (shows.contains(request.getPodcast().getShow())) {
                showMatch = 1.0;
            } else {
                showMatch = 0.4; // Partial match: campaign targets shows but not this show
            }
        }

        // Listener segment weight: premium tier and smart speakers are higher value
        if (request.getListener().getTier() == com.podads.domain.valueobjects.TierType.PREMIUM) {
            listenerSegmentWeight = 1.1; // Premium listeners worth 10% more
        }
        if (request.getListener().getDevice() == com.podads.domain.valueobjects.DeviceType.SMART_SPEAKER) {
            listenerSegmentWeight *= 1.05; // Smart speakers worth 5% more (multiplicative)
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categoryMatch", categoryMatch);
        result.put("showMatch", showMatch);
        result.put("listenerSegmentWeight", listenerSegmentWeight);
        return result;
    }

    public double calculatePacingMultiplier(CandidateAd candidate) {
        var pacing = candidate.getCampaign().getPacing();
        if (pacing.getDailyBudget() == null) {
            return 1.0; // No pacing limit = no throttling
        }

        double spendRatio = (pacing.getDailySpend() != null ? pacing.getDailySpend() : 0.0)
                / (double) pacing.getDailyBudget();

        // Pacing throttling: prevent budget exhaustion by reducing score as spend approaches limit
        // Thresholds: 70% (moderate), 90% (heavy), 100% (complete)
        if (spendRatio >= 1.0) {
            return 0.0; // Budget exhausted - completely throttled
        }
        if (spendRatio > 0.9) {
            return 0.3; // Near limit - heavily throttled (70% score reduction)
        }
        if (spendRatio > 0.7) {
            return 0.7; // Approaching limit - moderately throttled (30% score reduction)
        }
        return 1.0; // Under 70% spend - no throttling
    }

    public Map<String, Object> scoreCandidate(AdRequest request, CandidateAd candidate) {
        int bidCpm = candidate.getCampaign().getBidCpm();
        Map<String, Object> matchComponents = calculateMatchScore(request, candidate);
        double categoryMatch = (Double) matchComponents.get("categoryMatch");
        double showMatch = (Double) matchComponents.get("showMatch");
        double listenerSegmentWeight = (Double) matchComponents.get("listenerSegmentWeight");
        double pacingMultiplier = calculatePacingMultiplier(candidate);

        // Match score: weighted average (category 60%, show 40%)
        // Show match is multiplied by listenerSegmentWeight (premium/smart speaker boost)
        double matchScore = categoryMatch * 0.6 + showMatch * 0.4 * listenerSegmentWeight;

        // Final auction score: bidCPM × matchScore × pacingMultiplier
        // Higher score = better candidate. Winner is highest scorer.
        double finalScore = bidCpm * matchScore * pacingMultiplier;

        Map<String, Object> score = new HashMap<>();
        score.put("bidCpm", (double) bidCpm);
        score.put("matchScore", matchScore);
        score.put("pacingMultiplier", pacingMultiplier);
        score.put("finalScore", finalScore);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("categoryMatch", categoryMatch);
        breakdown.put("showMatch", showMatch);
        breakdown.put("listenerSegmentWeight", listenerSegmentWeight);
        score.put("breakdown", breakdown);

        return score;
    }
}


