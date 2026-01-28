package com.podads.infrastructure.filters;

import com.podads.domain.entities.*;
import com.podads.domain.valueobjects.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder for creating AdRequest and CandidateAd objects for filter testing.
 */
public class TestDataBuilder {

    public static class AdRequestBuilder {
        private String requestId = "test-request-1";
        private PodcastCategory category = PodcastCategory.FITNESS;
        private String show = "Test Show";
        private String episode = "ep-001";
        private SlotType slotType = SlotType.MID_ROLL;
        private Integer cuePoint = 300;
        private String geo = "US";
        private DeviceType device = DeviceType.MOBILE;
        private TierType tier = TierType.FREE;
        private Boolean consent = true;
        private TimeOfDay timeOfDay = TimeOfDay.AFTERNOON;
        private Instant timestamp = Instant.now();

        public AdRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public AdRequestBuilder category(PodcastCategory category) {
            this.category = category;
            return this;
        }

        public AdRequestBuilder show(String show) {
            this.show = show;
            return this;
        }

        public AdRequestBuilder episode(String episode) {
            this.episode = episode;
            return this;
        }

        public AdRequestBuilder slotType(SlotType slotType) {
            this.slotType = slotType;
            return this;
        }

        public AdRequestBuilder cuePoint(Integer cuePoint) {
            this.cuePoint = cuePoint;
            return this;
        }

        public AdRequestBuilder geo(String geo) {
            this.geo = geo;
            return this;
        }

        public AdRequestBuilder device(DeviceType device) {
            this.device = device;
            return this;
        }

        public AdRequestBuilder tier(TierType tier) {
            this.tier = tier;
            return this;
        }

        public AdRequestBuilder consent(Boolean consent) {
            this.consent = consent;
            return this;
        }

        public AdRequestBuilder timeOfDay(TimeOfDay timeOfDay) {
            this.timeOfDay = timeOfDay;
            return this;
        }

        public AdRequestBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AdRequest build() {
            return new AdRequest(
                    requestId,
                    new PodcastContext(category, show, episode),
                    new SlotContext(slotType, cuePoint),
                    new ListenerContext(geo, device, tier, consent, timeOfDay),
                    timestamp
            );
        }
    }

    public static class CandidateAdBuilder {
        private String campaignId = "camp-001";
        private String advertiserId = "adv-001";
        private String campaignName = "Test Campaign";
        private Campaign.CampaignStatus status = Campaign.CampaignStatus.ACTIVE;
        private Integer budgetTotal = 100000; // $1000 in cents
        private Integer budgetRemaining = 50000; // $500 in cents
        private Integer bidCpm = 500; // $5 CPM in cents
        private Instant startDate = Instant.now().minusSeconds(86400); // 1 day ago
        private Instant endDate = Instant.now().plusSeconds(86400 * 30); // 30 days from now
        private List<PodcastCategory> targetCategories = List.of(PodcastCategory.FITNESS);
        private List<String> targetShows = new ArrayList<>();
        private List<String> targetGeo = List.of("US");
        private List<DeviceType> targetDevices = List.of(DeviceType.MOBILE);
        private List<TierType> targetTiers = List.of(TierType.FREE);
        private List<PodcastCategory> excludeCategories = new ArrayList<>();
        private Integer pacingDailyBudget = 1000; // in cents
        private Integer pacingDailySpend = 500; // in cents
        private Integer frequencyCapMaxImpressions = 3;
        private Integer frequencyCapWindowHours = 1; // hours
        private String creativeId = "creat-001";
        private String creativeAssetUrl = "https://example.com/ad.mp3";
        private Integer creativeDuration = 30;
        private Creative.ApprovalStatus creativeApprovalStatus = Creative.ApprovalStatus.APPROVED;
        private List<SlotType> eligibleSlotTypes = List.of(SlotType.MID_ROLL);

        public CandidateAdBuilder campaignId(String campaignId) {
            this.campaignId = campaignId;
            return this;
        }

        public CandidateAdBuilder advertiserId(String advertiserId) {
            this.advertiserId = advertiserId;
            return this;
        }

        public CandidateAdBuilder campaignName(String campaignName) {
            this.campaignName = campaignName;
            return this;
        }

        public CandidateAdBuilder status(Campaign.CampaignStatus status) {
            this.status = status;
            return this;
        }

        public CandidateAdBuilder budgetTotal(Integer budgetTotal) {
            this.budgetTotal = budgetTotal;
            return this;
        }

        public CandidateAdBuilder budgetRemaining(Integer budgetRemaining) {
            this.budgetRemaining = budgetRemaining;
            return this;
        }

        public CandidateAdBuilder bidCpm(Integer bidCpm) {
            this.bidCpm = bidCpm;
            return this;
        }

        public CandidateAdBuilder startDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }

        public CandidateAdBuilder endDate(Instant endDate) {
            this.endDate = endDate;
            return this;
        }

        public CandidateAdBuilder targetCategories(List<PodcastCategory> targetCategories) {
            this.targetCategories = targetCategories;
            return this;
        }

        public CandidateAdBuilder targetShows(List<String> targetShows) {
            this.targetShows = targetShows;
            return this;
        }

        public CandidateAdBuilder targetGeo(List<String> targetGeo) {
            this.targetGeo = targetGeo;
            return this;
        }

        public CandidateAdBuilder targetDevices(List<DeviceType> targetDevices) {
            this.targetDevices = targetDevices;
            return this;
        }

        public CandidateAdBuilder targetTiers(List<TierType> targetTiers) {
            this.targetTiers = targetTiers;
            return this;
        }

        public CandidateAdBuilder excludeCategories(List<PodcastCategory> excludeCategories) {
            this.excludeCategories = excludeCategories;
            return this;
        }

        public CandidateAdBuilder pacingDailyBudget(Integer pacingDailyBudget) {
            this.pacingDailyBudget = pacingDailyBudget;
            return this;
        }

        public CandidateAdBuilder pacingDailySpend(Integer pacingDailySpend) {
            this.pacingDailySpend = pacingDailySpend;
            return this;
        }

        public CandidateAdBuilder frequencyCapMaxImpressions(Integer frequencyCapMaxImpressions) {
            this.frequencyCapMaxImpressions = frequencyCapMaxImpressions;
            return this;
        }

        public CandidateAdBuilder frequencyCapWindowHours(Integer frequencyCapWindowHours) {
            this.frequencyCapWindowHours = frequencyCapWindowHours;
            return this;
        }

        public CandidateAdBuilder creativeId(String creativeId) {
            this.creativeId = creativeId;
            return this;
        }

        public CandidateAdBuilder creativeAssetUrl(String creativeAssetUrl) {
            this.creativeAssetUrl = creativeAssetUrl;
            return this;
        }

        public CandidateAdBuilder creativeDuration(Integer creativeDuration) {
            this.creativeDuration = creativeDuration;
            return this;
        }

        public CandidateAdBuilder creativeApprovalStatus(Creative.ApprovalStatus creativeApprovalStatus) {
            this.creativeApprovalStatus = creativeApprovalStatus;
            return this;
        }

        public CandidateAdBuilder eligibleSlotTypes(List<SlotType> eligibleSlotTypes) {
            this.eligibleSlotTypes = eligibleSlotTypes;
            return this;
        }

        public CandidateAd build() {
            Budget budget = new Budget(budgetTotal, budgetRemaining);
            Pacing pacing = new Pacing(pacingDailyBudget, pacingDailySpend);
            FrequencyCap frequencyCap = new FrequencyCap(frequencyCapMaxImpressions, frequencyCapWindowHours);
            TargetingRule targeting = new TargetingRule(
                    targetGeo,
                    targetDevices,
                    targetTiers,
                    targetCategories,
                    targetShows,
                    excludeCategories
            );
            Creative creative = new Creative(
                    creativeId,
                    campaignId,
                    creativeDuration,
                    creativeAssetUrl,
                    creativeApprovalStatus
            );
            Campaign campaign = new Campaign(
                    campaignId,
                    advertiserId,
                    campaignName,
                    status,
                    budget,
                    bidCpm,
                    startDate,
                    endDate,
                    targeting,
                    pacing,
                    frequencyCap
            );
            return new CandidateAd(campaign, creative, eligibleSlotTypes);
        }
    }

    public static AdRequestBuilder adRequest() {
        return new AdRequestBuilder();
    }

    public static CandidateAdBuilder candidateAd() {
        return new CandidateAdBuilder();
    }
}
