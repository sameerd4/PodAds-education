package com.podads.infrastructure.sourcing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podads.domain.entities.*;
import com.podads.domain.valueobjects.PodcastCategory;
import com.podads.domain.valueobjects.SlotType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FixtureSourcingService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CandidateAd> loadCandidates() {
        List<Campaign> campaigns = loadCampaigns();
        List<Creative> creatives = loadCreatives();

        List<CandidateAd> candidates = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            List<Creative> campaignCreatives = creatives.stream()
                    .filter(c -> c.getCampaignId().equals(campaign.getId()))
                    .collect(Collectors.toList());

            for (Creative creative : campaignCreatives) {
                // Get eligible slot types from creative data
                List<SlotType> eligibleSlots = getEligibleSlotTypes(creative.getId());
                candidates.add(new CandidateAd(campaign, creative, eligibleSlots));
            }
        }
        return candidates;
    }

    public List<CandidateAd> filterCandidatesByCategory(List<CandidateAd> candidates, PodcastCategory category) {
        return candidates.stream()
                .filter(candidate -> {
                    var categories = candidate.getCampaign().getTargeting().getCategories();
                    return categories == null || categories.isEmpty() || categories.contains(category);
                })
                .collect(Collectors.toList());
    }

    private List<Campaign> loadCampaigns() {
        try {
            ClassPathResource resource = new ClassPathResource("fixtures/campaigns.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode campaignsNode = root.get("campaigns");

            List<Campaign> campaigns = new ArrayList<>();
            for (JsonNode campNode : campaignsNode) {
                Campaign campaign = new Campaign();
                campaign.setId(campNode.get("id").asText());
                campaign.setAdvertiserId(campNode.get("advertiserId").asText());
                campaign.setName(campNode.get("name").asText());
                campaign.setStatus(Campaign.CampaignStatus.valueOf(campNode.get("status").asText().toUpperCase()));

                JsonNode budgetNode = campNode.get("budget");
                campaign.setBudget(new Budget(
                        budgetNode.get("total").asInt(),
                        budgetNode.get("remaining").asInt()
                ));

                campaign.setBidCpm(campNode.get("bidCpm").asInt());
                campaign.setStartDate(Instant.parse(campNode.get("startDate").asText()));
                campaign.setEndDate(Instant.parse(campNode.get("endDate").asText()));

                JsonNode targetingNode = campNode.get("targeting");
                TargetingRule targeting = new TargetingRule();
                if (targetingNode.has("geo")) {
                    targeting.setGeo(objectMapper.convertValue(targetingNode.get("geo"), List.class));
                }
                if (targetingNode.has("device")) {
                    List<String> deviceStrings = objectMapper.convertValue(targetingNode.get("device"), List.class);
                    targeting.setDevice(deviceStrings.stream()
                            .map(d -> com.podads.domain.valueobjects.DeviceType.fromString(d))
                            .collect(Collectors.toList()));
                }
                if (targetingNode.has("tier")) {
                    List<String> tierStrings = objectMapper.convertValue(targetingNode.get("tier"), List.class);
                    targeting.setTier(tierStrings.stream()
                            .map(t -> com.podads.domain.valueobjects.TierType.fromString(t))
                            .collect(Collectors.toList()));
                }
                if (targetingNode.has("categories")) {
                    List<String> catStrings = objectMapper.convertValue(targetingNode.get("categories"), List.class);
                    targeting.setCategories(catStrings.stream()
                            .map(c -> PodcastCategory.fromString(c))
                            .collect(Collectors.toList()));
                }
                if (targetingNode.has("shows")) {
                    targeting.setShows(objectMapper.convertValue(targetingNode.get("shows"), List.class));
                }
                if (targetingNode.has("excludeCategories")) {
                    List<String> exclCatStrings = objectMapper.convertValue(targetingNode.get("excludeCategories"), List.class);
                    targeting.setExcludeCategories(exclCatStrings.stream()
                            .map(c -> PodcastCategory.fromString(c))
                            .collect(Collectors.toList()));
                }
                campaign.setTargeting(targeting);

                JsonNode pacingNode = campNode.get("pacing");
                Pacing pacing = new Pacing();
                if (pacingNode.has("dailyBudget")) {
                    pacing.setDailyBudget(pacingNode.get("dailyBudget").asInt());
                }
                if (pacingNode.has("dailySpend")) {
                    pacing.setDailySpend(pacingNode.get("dailySpend").asInt());
                }
                campaign.setPacing(pacing);

                if (campNode.has("frequencyCap")) {
                    JsonNode freqCapNode = campNode.get("frequencyCap");
                    campaign.setFrequencyCap(new FrequencyCap(
                            freqCapNode.get("maxImpressions").asInt(),
                            freqCapNode.get("windowHours").asInt()
                    ));
                }

                campaigns.add(campaign);
            }
            return campaigns;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load campaigns", e);
        }
    }

    private List<Creative> loadCreatives() {
        try {
            ClassPathResource resource = new ClassPathResource("fixtures/creatives.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode creativesNode = root.get("creatives");

            List<Creative> creatives = new ArrayList<>();
            for (JsonNode creatNode : creativesNode) {
                Creative creative = new Creative();
                creative.setId(creatNode.get("id").asText());
                creative.setCampaignId(creatNode.get("campaignId").asText());
                creative.setDurationSeconds(creatNode.get("durationSeconds").asInt());
                creative.setAssetUrl(creatNode.get("assetUrl").asText());
                creative.setApprovalStatus(Creative.ApprovalStatus.valueOf(
                        creatNode.get("approvalStatus").asText().toUpperCase()));
                creatives.add(creative);
            }
            return creatives;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load creatives", e);
        }
    }

    private List<SlotType> getEligibleSlotTypes(String creativeId) {
        try {
            ClassPathResource resource = new ClassPathResource("fixtures/creatives.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode creativesNode = root.get("creatives");

            for (JsonNode creatNode : creativesNode) {
                if (creatNode.get("id").asText().equals(creativeId)) {
                    if (creatNode.has("eligibleSlotTypes")) {
                        List<String> slotStrings = objectMapper.convertValue(creatNode.get("eligibleSlotTypes"), List.class);
                        return slotStrings.stream()
                                .map(s -> SlotType.fromString(s))
                                .collect(Collectors.toList());
                    }
                    break;
                }
            }
            // Default
            return List.of(SlotType.PRE_ROLL, SlotType.MID_ROLL, SlotType.POST_ROLL);
        } catch (Exception e) {
            return List.of(SlotType.PRE_ROLL, SlotType.MID_ROLL, SlotType.POST_ROLL);
        }
    }
}


